namespace TrackingSystem.Models
{
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;

    using static TrackingSystem.Common.Constants;

    public class TargetIdentifier
    {
        public int Id { get; set; }

        [Required]
        [Index("IX_Identifier", 1, IsUnique = true)]
        [MinLength(MinLengthIdentifier)]
        [MaxLength(MaxLengthStringField)]
        public string Identifier { get; set; }
    }
}
