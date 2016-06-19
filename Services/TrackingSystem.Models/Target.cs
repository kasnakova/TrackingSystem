namespace TrackingSystem.Models
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using static TrackingSystem.Common.Constants;

    public class Target
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
        public DateTime DateCreated { get; set; }

        [Required]
        public DateTime DateUpdated { get; set; }

        [Required]
        public bool Active { get; set; }
        
        public bool ShouldNotMove { get; set; }
        
        public DateTime? ShouldNotMoveUntil { get; set; }

        public bool NotificationSent { get; set; }

        [Required]
        public bool Deleted { get; set; }

        [Required]
        [Key, Column(Order = 0)]
        public int TargetIdentifierId { get; set; }

        public virtual TargetIdentity TargetIdentifier { get; set; }

        [Required]
        [Key, Column(Order = 1)]
        public string UserId { get; set; }

        public virtual ApplicationUser User { get; set; }
    }
}
