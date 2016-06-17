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
                return BadRequest(ModelState);
            }

            var targetIdentifier = data.TargetIdentifiers.All().Where(t => t.Identifier == targetModel.Identifier).FirstOrDefault();
            if (targetIdentifier == null)
            {
                return BadRequest("No such target! Please first install the special software on the target.");
            }

            var userId = userIdProvider.GetUserId();
            var target = data.Targets.All().Where(t => (t.TargetIdentifierId == targetIdentifier.Id) && (t.UserId == userId) && !t.Deleted).FirstOrDefault();
            if (target != null)
            {
                return BadRequest("You are already tracking this target!");
            }

            var newTarget = new Target()
            {
                Active = true,
                Deleted = false,
                TargetIdentifierId = targetIdentifier.Id,
                UserId = userId,
                Name = targetModel.Name,
                Type = targetModel.Type,
                ShouldNotMove = false,
                DateCreated = DateTime.Now,
                DateUpdated = DateTime.Now
            };
            
            data.Targets.Add(newTarget);
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

            target.Deleted = true;
            data.Targets.Update(target);
            data.SaveChanges();
            return Ok();
        }

        [HttpGet]
        public IHttpActionResult GetTargets()
        {
            var currentUserId = userIdProvider.GetUserId();
            var targets = data.Targets.All().Where(n => (n.UserId == currentUserId) && (!n.Deleted)).Select(TargetViewModel.FromTarget);
            return Ok(targets);
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

            var positions = data.Positions.All().Where(p => p.TargetIdentifierId == targetId && DbFunctions.TruncateTime(p.DateTime) == date.Date).OrderBy(p => p.DateTime).Select(HistoryPositionViewModel.FromPosition);
            
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
                //push to device to isActive sending coordinates via GCM
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

            data.Targets.Update(target);
            data.SaveChanges();

            if (shouldNotMove)
            {

            }

            return Ok();
        }

        [HttpGet]
        public IHttpActionResult TurnAlarmOn(int id)
        {
            Target target;
            if(!IsTargetValid(id, out target))
            {
                return BadRequest(TextNotValidTarget);
            }

            //push to device via GCM
            return Ok();
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