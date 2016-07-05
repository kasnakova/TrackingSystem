namespace TrackingSystem.Services.Models
{
    using System.ComponentModel.DataAnnotations;

    using static TrackingSystem.Common.Constants;

    public class PositionBindingModel
    {
        [Required]
        public string Latitude { get; set; }

        [Required]
        public string Longitude { get; set; }

        [Required]
        [MinLength(MinLengthIdentifier)]
        [MaxLength(MaxLengthStringField)]
        public string Identifier { get; set; }
    }
}