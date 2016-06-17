namespace TrackingSystem.Services.Infrastructure
{
    using Microsoft.AspNet.Identity;
    using System.Threading;

    public class AspNetUserIdProvider : IUserIdProvider
    {
        public string GetUserId()
        {
            return Thread.CurrentPrincipal.Identity.GetUserId();
        }
    }
}