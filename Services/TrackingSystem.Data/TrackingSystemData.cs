namespace TrackingSystem.Data
{
    using System.Data.Entity;
    using System.Collections.Generic;
    using System;

    using TrackingSystem.Data.Repositories;
    using TrackingSystem.Models;

    public class TrackingSystemData : ITrackingSystemData
    {
        private DbContext context;

        private IDictionary<Type, object> repositories;

        public TrackingSystemData(DbContext context)
        {
            this.context = context;
            this.repositories = new Dictionary<Type, object>();
        }

        public IRepository<ApplicationUser> Users
        {
            get { return this.GetRepository<ApplicationUser>(); }
        }

        public IRepository<Target> Targets
        {
            get { return this.GetRepository<Target>(); }
        }


        public IRepository<TargetIdentity> TargetIdentifiers
        {
            get { return this.GetRepository<TargetIdentity>(); }
        }
        
        public IRepository<Position> Positions
        {
            get { return this.GetRepository<Position>(); }
        }

        public int SaveChanges()
        {
            return this.context.SaveChanges();
        }

        private IRepository<T> GetRepository<T>() where T : class
        {
            var typeOfRepository = typeof(T);
            if (!this.repositories.ContainsKey(typeOfRepository))
            {
                var newRepository = Activator.CreateInstance(typeof(EFRepository<T>), context);
                this.repositories.Add(typeOfRepository, newRepository);
            }

            return (IRepository<T>)this.repositories[typeOfRepository];
        }
    }
}
