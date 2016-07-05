namespace TrackingSystem.Services.Controllers
{
    using System;
    using System.Linq;
    using System.Web.Http;
    using System.Collections.Generic;
    using System.Data.Entity;

    using TrackingSystem.Data;
    using TrackingSystem.Models;
    using TrackingSystem.Services.Models;
    using TrackingSystem.Services.Infrastructure;
    using static TrackingSystem.Common.Constants;

    [Authorize]
    public class TargetController : BaseApiController
    {
        private IUserIdProvider userIdProvider;

        public TargetController()
            : this(new TrackingSystemData(TrackingSystemDbContext.Create()), new AspNetUserIdProvider())
        {

        }

        public TargetController(ITrackingSystemData data,
                                IUserIdProvider userIdProvider)
            : base(data)
        {
            this.userIdProvider = userIdProvider;
        }
        
        [HttpPost]
        public IHttpActionResult AddTarget(TargetBindingModel targetModel)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelStatePrettifier.Prettify(ModelState));
            }

            var targetIdentifier = data.TargetIdentifiers.All().Where(t => t.Identifier == targetModel.Identifier).FirstOrDefault();
            if (targetIdentifier == null)
            {
                return BadRequest("No such target! Please first install the special software on the target.");
            }

            var userId = userIdProvider.GetUserId();
            var target = data.Targets.All().Where(t => (t.TargetIdentifierId == targetIdentifier.Id) && (t.UserId == userId)).FirstOrDefault();
            if (target != null && !target.Deleted)
            {
                return BadRequest("You are already tracking this target!");
            }

            bool wasTargetDeleted = false;
            if (target == null)
            {
                target = new Target();
            }
            else
            {
                wasTargetDeleted = true;
            }


            target.Active = true;
            target.Deleted = false;
            target.TargetIdentifierId = targetIdentifier.Id;
            target.UserId = userId;
            target.Name = targetModel.Name;
            target.Type = targetModel.Type;
            target.ShouldNotMove = false;
            target.DateCreated = DateTime.Now;
            target.DateUpdated = DateTime.Now;

            bool success = GCMProvider.SendMessage(targetIdentifier.GCMKey, PushMessageType.SetIsTargetActive, true.ToString());
            if (!success)
            {
                target.Active = false;
            }

            if (wasTargetDeleted)
            {
                data.Targets.Update(target);
            }
            else
            {
                data.Targets.Add(target);
            }

            data.SaveChanges();
            return Ok();
        }

        [HttpDelete]
        public IHttpActionResult DeleteTarget(int id)
        {
            Target target;
            if (!IsTargetValid(id, out target))
            {
                return BadRequest(TextNotValidTarget);
            }

            bool success = GCMProvider.SendMessage(target.TargetIdentifier.GCMKey, PushMessageType.SetIsTargetActive, false.ToString());
            target.Active = !success;
            target.Deleted = true;
            data.Targets.Update(target);
            data.SaveChanges();
            return Ok();
        }

        [HttpGet]
        public IHttpActionResult GetTargets()
        {
            var currentUserId = userIdProvider.GetUserId();
            var targets = data.Targets.All().Where(n => (n.UserId == currentUserId) && (!n.Deleted));
            foreach (var target in targets)
            {
                if(target.ShouldNotMove && target.ShouldNotMoveUntil < DateTime.Now)
                {
                    target.ShouldNotMove = false;
                    data.Targets.Update(target);
                }
            }

            data.SaveChanges();
            var viewModelTargets = targets.Select(TargetViewModel.FromTarget);
            return Ok(viewModelTargets);
        }

        [HttpGet]
        public IHttpActionResult GetTargetsPosition()
        {
            var currentUserId = userIdProvider.GetUserId();
            var targets = data.Targets.All().Where(t => t.UserId == currentUserId && !t.Deleted && t.Active);
            var positions = new List<PositionViewModel>();

            foreach (var target in targets)
            {
                var latestPosition = data.Positions.All()
                    .Where(p => p.TargetIdentifierId == target.TargetIdentifierId)
                    .OrderByDescending(p => p.DateTime)
                    .Take(1)
                    .Select(PositionViewModel.FromPosition)
                    .FirstOrDefault();

                if(latestPosition != null)
                {
                    latestPosition.TargetName = target.Name;
                    positions.Add(latestPosition);
                }
            }
            
            return Ok(positions);
        }

        [HttpGet]
        public IHttpActionResult GetHistoryOfPositions(int targetId, DateTime date)
        {
            Target target;
            if (!IsTargetValid(targetId, out target))
            {
                return BadRequest(TextNotValidTarget);
            }

            var positions = data.Positions.All()
                .Where(p => p.TargetIdentifierId == targetId && DbFunctions.TruncateTime(p.DateTime) == date.Date)
                .OrderBy(p => p.DateTime)
                .ToList()
                .Select(HistoryPositionViewModel.FromPosition);
            
            return Ok(positions);
        }
        
        [HttpGet]
        public IHttpActionResult SetIsTargetActive(int id, bool isActive)
        {
            Target target;
            if (!IsTargetValid(id, out target))
            {
                return BadRequest(TextNotValidTarget);
            }

            target.Active = isActive;
            data.Targets.Update(target);
            data.SaveChanges();

            var trackingsCount = data.Targets.All().Count(t => t.TargetIdentifierId == id && t.Active);
            if (isActive)
            {
                trackingsCount--; //when isActive is true, it will always return at least one, because of the above update
            }

            if (trackingsCount == 0)
            {
                bool success = GCMProvider.SendMessage(target.TargetIdentifier.GCMKey, PushMessageType.SetIsTargetActive, isActive.ToString());
                if (!success)
                {
                    target.Active = !isActive;
                    data.Targets.Update(target);
                    data.SaveChanges();
                    return BadRequest();
                }
            }

            return Ok();
        }

        [HttpGet]
        public IHttpActionResult SetShouldTargetMove(int id, bool shouldNotMove, DateTime shouldNotMoveUntil)
        {
            Target target;
            if (!IsTargetValid(id, out target))
            {
                return BadRequest(TextNotValidTarget);
            }

            target.ShouldNotMove = shouldNotMove;
            target.ShouldNotMoveUntil = shouldNotMoveUntil;
            target.NotificationSent = false;

            data.Targets.Update(target);
            data.SaveChanges();

            return Ok();
        }

        [HttpGet]
        public IHttpActionResult TurnAlarmOn(int id)
        {
            Target target;
            if (!IsTargetValid(id, out target))
            {
                return BadRequest(TextNotValidTarget);
            }

            var currUserId = userIdProvider.GetUserId();
            var userName = data.Users.Find(currUserId).UserName;

            if (GCMProvider.SendMessage(target.TargetIdentifier.GCMKey, PushMessageType.TurnAlarmOn, userName))
            {
                return Ok();
            } else
            {
                return BadRequest();
            }
        }

        //TODO: For test purposes only
        [HttpGet]
        public IHttpActionResult ChangeLocationUpdatesInterval(long interval)
        {
            //var currUserId = userIdProvider.GetUserId();
            //var targets

            //if (GCMProvider.SendMessage(target.TargetIdentifier.GCMKey, PushMessageType.ChangeLocationInterval, interval.ToString()))
            //{
            //    return Ok();
            //}
            //else
            {
                return BadRequest();
            }
        }

        private bool IsTargetValid(int id, out Target target)
        {
            var currentUserId = userIdProvider.GetUserId();
            target = data.Targets.All().Where(t => (t.UserId == currentUserId) && (t.TargetIdentifierId == id) && (!t.Deleted)).FirstOrDefault();
            if (target == null)
            {
                return false;
            }

            return true;
        }
    }
}