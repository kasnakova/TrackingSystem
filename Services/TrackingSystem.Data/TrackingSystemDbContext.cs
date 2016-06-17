namespace TrackingSystem.Data
{
    using System.Data.Entity;

    using Microsoft.AspNet.Identity.EntityFramework;

    using TrackingSystem.Models;
    using TrackingSystem.Data.Migrations;

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

        public IDbSet<TargetIdentifier> TargetIdentifiers { get; set; }

        public IDbSet<Position> Positions { get; set; }
    }
}
