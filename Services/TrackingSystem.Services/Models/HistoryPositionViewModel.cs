namespace TrackingSystem.Services.Models
{
    using System;
    using System.Linq.Expressions;

    using TrackingSystem.Models;

    public class HistoryPositionViewModel
    {
        public static Func<Position, HistoryPositionViewModel> FromPosition
        {
            get
            {
                return p => new HistoryPositionViewModel
                {
                    Latitude = p.Latitude,
                    Longitude = p.Longitude,
                    Time = p.DateTime.ToString("HH:mm")
                };
            }
        }

        public double Latitude { get; set; }

        public double Longitude { get; set; }

        public string Time { get; set; }
    }
}