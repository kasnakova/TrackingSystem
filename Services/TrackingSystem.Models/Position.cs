namespace TrackingSystem.Models
{
    using System;
    using System.ComponentModel.DataAnnotations;

    using static TrackingSystem.Common.Constants;

    public class Position
    {
        public int Id { get; set; }

        [Required]
        [Range(MinLatitude, MaxLatitude)]
        public double Latitude { get; set; }

        [Required]
        [Range(MinLongitude, MaxLongitude)]
        public double Longitude { get; set; }

        [Required]
        public DateTime DateTime { get; set; }

        [Required]
        public int TargetIdentifierId { get; set; }

        public virtual TargetIdentity TargetIdentifier { get; set; }
    }
}
