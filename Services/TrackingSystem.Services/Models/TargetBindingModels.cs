namespace TrackingSystem.Services.Models
{
    using System;
    using System.ComponentModel.DataAnnotations;
    
    using static TrackingSystem.Common.Constants;

    public class TargetBindingModel
    {
        [Required]
        [MinLength(MinLengthStringField)]
        [MaxLength(MaxLengthStringField)]
        public string Type { get; set; }

        [Required]
        [MinLength(MinLengthStringField)]
        [MaxLength(MaxLengthStringField)]
        public string Name { get; set; }

        [Required]
        [MinLength(MinLengthIdentifier)]
        [MaxLength(MaxLengthStringField)]
        public string Identifier { get; set; }
    }
}