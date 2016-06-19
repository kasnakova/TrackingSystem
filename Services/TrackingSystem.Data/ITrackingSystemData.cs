namespace TrackingSystem.Data
{
    using TrackingSystem.Data.Repositories;
    using TrackingSystem.Models;

    public interface ITrackingSystemData
    {
        IRepository<ApplicationUser> Users { get; }

        IRepository<Target> Targets { get; }

        IRepository<TargetIdentity> TargetIdentifiers { get; }

        IRepository<Position> Positions { get; }

        int SaveChanges();
    }
}
