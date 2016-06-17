namespace TrackingSystem.Services.Models
{
    using System;
    using System.Linq.Expressions;

    using TrackingSystem.Models;

    public class TargetViewModel
    {
        public static Expression<Func<Target, TargetViewModel>> FromTarget
        {
            get
            {
                return t => new TargetViewModel
                {
                    Id = t.TargetIdentifierId,
                    Type = t.Type,
                    Name = t.Name,
                    Active = t.Active
                };
            }
        }

        public int Id { get; set; }

        public string Type { get; set; }

        public string Name { get; set; }

        public bool Active { get; set; }
    }
}