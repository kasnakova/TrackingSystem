namespace TrackingSystem.Services.Controllers
{
    using System;
    using System.Linq;
    using System.Web.Http;

    using TrackingSystem.Data;
    
    public abstract class BaseApiController : ApiController
    {
        protected ITrackingSystemData data;

        protected BaseApiController(ITrackingSystemData data)
        {
            this.data = data;
        }
    }
}