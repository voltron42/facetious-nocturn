using facetious_nocturn.Models;
using facetious_nocturn.Services;
using Microsoft.AspNetCore.Mvc;
using System.Net;

namespace facetious_nocturn.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class SharedSessionController : ControllerBase
    {
        private readonly ILogger<SharedSessionController> _logger;
        private readonly ISharedSessionService<object, object, object> _sharedSessionService;

        public SharedSessionController(ILogger<SharedSessionController> logger, ISharedSessionService<object, object, object> sharedSessionService)
        {
            _logger = logger;
            _sharedSessionService = sharedSessionService;
        }

        [HttpPost]
        [Route("Host")]
        public IActionResult Host([FromBody] Session<object,object,object> newSession)
        {
            return ProcessRequest((ipAddress) =>
            {
                return _sharedSessionService.Host(ipAddress, newSession.Host.NameTag, newSession.Host.State, newSession.Context);
            });
        }

        [HttpPost]
        [Route("Join/{key}")]
        public IActionResult Join(string sessionKey, [FromBody] Guest<object> newGuest)
        {
            return ProcessRequest((ipAddress) => _sharedSessionService.Join(sessionKey, ipAddress, newGuest.NameTag, newGuest.State));
        }

        [HttpDelete]
        [Route("Session/{sessionId}/Kick/{guestIP}")]
        public IActionResult Kick(string sessionId, string guestIP)
        {
            return ProcessRequest((ipAddress) => _sharedSessionService.Kick(sessionId, ipAddress, guestIP));
        }

        [HttpDelete]
        [Route("Session/{sessionId}/Leave")]
        public IActionResult Leave(string sessionId)
        {
            return ProcessRequest((ipAddress) => _sharedSessionService.Leave(sessionId, ipAddress));
        }

        [HttpDelete]
        [Route("Session/{sessionId}/Close")]
        public IActionResult Close(string sessionId)
        {
            return ProcessRequest((ipAddress) => _sharedSessionService.Close(sessionId, ipAddress));
        }

        [HttpGet]
        [Route("Session/{sessionId}/HostData")]
        public IActionResult GetHostData(string sessionId)
        {
            return ProcessRequest((ipAddress) => _sharedSessionService.GetHostData(sessionId, ipAddress));
        }

        [HttpGet]
        [Route("Session/{sessionId}/UserData")]
        public IActionResult GetUserData(string sessionId)
        {
            return ProcessRequest((ipAddress) => _sharedSessionService.GetUserData(sessionId, ipAddress));
        }


        [HttpPost]
        [Route("Session/{sessionId}/HostData")]
        public IActionResult SetHostData(string sessionId, [FromBody] Session<object, object, object> session)
        {
            return ProcessRequest((ipAddress) => _sharedSessionService.SetHostData(sessionId, ipAddress, session));
        }

        [HttpPost]
        [Route("Session/{sessionId}/UserData")]
        public IActionResult SetUserData(string sessionId, [FromBody] UserData<object, object> userData)
        {
            return ProcessRequest((ipAddress) => _sharedSessionService.SetUserData(sessionId, ipAddress, userData));
        }
        
        private IActionResult ProcessRequest(Func<IPAddress,object> _func)
        {
            try
            {
                return Ok(_func(Request.HttpContext.Connection.RemoteIpAddress));
            } catch (Exception ex) {
                return BadRequest(ex.Message);
            }
        }
    }
}
