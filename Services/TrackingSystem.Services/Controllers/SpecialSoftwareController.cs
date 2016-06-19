namespace TrackingSystem.Services.Controllers
{
    using System;
    using System.Linq;
    using System.Web.Http;
    using TrackingSystem.Data;
    using TrackingSystem.Models;
    using Models;
    using static TrackingSystem.Common.Constants;

    public class SpecialSoftwareController : BaseApiController
    {
        public SpecialSoftwareController()
            : this(new TrackingSystemData(TrackingSystemDbContext.Create()))
        {

        }

        public SpecialSoftwareController(ITrackingSystemData data)
            : base(data)
        {

        }

        [HttpPost]
        public IHttpActionResult RegisterIdentifier(TargetIdentityBindingModel targetIdentity)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest();
            }

            var existingIdentifier = data.TargetIdentifiers.All()
                .Where(i => i.Identifier == targetIdentity.Identifier || i.GCMKey == targetIdentity.GCMKey).FirstOrDefault();

            if(existingIdentifier != null)
            {
                return BadRequest();
            }

            var targetIdentifier = new TargetIdentity()
            {
                Identifier = targetIdentity.Identifier,
                GCMKey = targetIdentity.GCMKey,
                DateCreated = DateTime.Now
            };

            data.TargetIdentifiers.Add(targetIdentifier);
            data.SaveChanges();
            return Ok();
        }

        [HttpPost]
        public IHttpActionResult ReceiveCoordinates(PositionBindingModel positionModel)
        {
            var existingIdentifier = data.TargetIdentifiers.All().Where(i => i.Identifier == positionModel.Identifier).FirstOrDefault();
            if (existingIdentifier == null || !ModelState.IsValid)
            {
                return BadRequest();
            }

            var newPosition = new Position()
            {
                TargetIdentifierId = existingIdentifier.Id,
                Latitude = positionModel.Latitude,
                Longitude = positionModel.Longitude,
                DateTime = DateTime.Now
            };

            var shouldNotMoveTrackers = data.Targets.All()
                .Where(t => t.TargetIdentifierId == existingIdentifier.Id && !t.Deleted && t.Active && t.ShouldNotMove && t.ShouldNotMoveUntil > DateTime.Now && !t.NotificationSent);
            if (shouldNotMoveTrackers.Count() > 0)
            {
                var lastPosition = data.Positions.All()
                    .Where(p => p.TargetIdentifierId == existingIdentifier.Id)
                    .OrderByDescending(p => p.DateTime)
                    .Take(1)
                    .FirstOrDefault();

                if (lastPosition.Latitude != newPosition.Latitude || lastPosition.Longitude != newPosition.Longitude)
                {
                    //send to users notification via GCM
                    foreach (var tracker in shouldNotMoveTrackers)
                    {
                        tracker.NotificationSent = true;
                        data.Targets.Update(tracker);
                    }
                }
            }
            
            data.Positions.Add(newPosition);
            data.SaveChanges();
            return Ok();
        }
    }
}