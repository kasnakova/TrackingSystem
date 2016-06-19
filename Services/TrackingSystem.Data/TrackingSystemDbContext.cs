namespace TrackingSystem.Data
{
    using System.Data.Entity;

    using Microsoft.AspNet.Identity.EntityFramework;

    using TrackingSystem.Models;
    using TrackingSystem.Data.Migrations;
    using System.Data.Entity.ModelConfiguration.Conventions;
    public class TrackingSystemDbContext : IdentityDbContext<ApplicationUser>
    {
        public TrackingSystemDbContext()
            : base("DefaultConnection", throwIfV1Schema: false)
        {
            Database.SetInitializer(new MigrateDatabaseToLatestVersion<TrackingSystemDbContext, Configuration>());
        }

        public static TrackingSystemDbContext Create()
        {
            return new TrackingSystemDbContext();
        }

        public IDbSet<Target> Targets { get; set; }

        public IDbSet<TargetIdentity> TargetIdentifiers { get; set; }

        public IDbSet<Position> Positions { get; set; }

        protected override void OnModelCreating(DbModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
            modelBuilder.Entity<Target>()
                .HasRequired(c => c.User)
                .WithMany()
                .WillCascadeOnDelete(false);

            modelBuilder.Entity<Target>()
                .HasRequired(s => s.TargetIdentifier)
                .WithMany()
                .WillCascadeOnDelete(false);
        }
    }
}
