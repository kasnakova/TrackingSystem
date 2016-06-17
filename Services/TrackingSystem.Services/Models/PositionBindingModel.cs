namespace TrackingSystem.Services.Models
{
    using System.ComponentModel.DataAnnotations;

    using static TrackingSystem.Common.Constants;

    public class PositionBindingModel
    {
        [Required]
        [Range(MinLatitude, MaxLatitude)]
        public double Latitude { get; set; }

        [Required]
        [Range(MinLongitude, MaxLongitude)]
        public double Longitude { get; set; }

        [Required]
        [MinLength(MinLengthIdentifier)]
        [MaxLength(MaxLengthStringField)]
        public string Identifier { get; set; }
    }
}