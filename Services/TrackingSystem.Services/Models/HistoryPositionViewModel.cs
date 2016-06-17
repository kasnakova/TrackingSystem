namespace TrackingSystem.Services.Models
{
    using System;
    using System.Linq.Expressions;

    using TrackingSystem.Models;

    public class HistoryPositionViewModel
    {
        public static Expression<Func<Position, HistoryPositionViewModel>> FromPosition
        {
            get
            {
                return p => new HistoryPositionViewModel
                {
                    Latitude = p.Latitude,
                    Longitude = p.Longitude,
                    DateTime = p.DateTime
                };
            }
        }

        public double Latitude { get; set; }

        public double Longitude { get; set; }

        public DateTime DateTime { get; set; }
    }
}