namespace TrackingSystem.Services.Models
{
    using System;
    using System.Linq.Expressions;

    using TrackingSystem.Models;

    public class PositionViewModel
    {
        public static Expression<Func<Position, PositionViewModel>> FromPosition
        {
            get
            {
                return p => new PositionViewModel
                {
                    Latitude = p.Latitude,
                    Longitude = p.Longitude
                };
            }
        }

        public double Latitude { get; set; }

        public double Longitude { get; set; }

        public string TargetName { get; set; }
    }
}