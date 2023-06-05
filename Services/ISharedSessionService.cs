using facetious_nocturn.Models;
using System.Net;

namespace facetious_nocturn.Services
{
    public interface ISharedSessionService<R,S,T>
    {
        public Session<R, S, T> Host(IPAddress ipAddress, string nameTag, R hostState, S context);

        public Guest<T> Join(string sessionKey, IPAddress ipAddress, string nameTag, T guestState);

        public Guest<T> Kick(string sessionId, IPAddress hostIP, string guestKey);

        public Guest<T> Leave(string sessionId, IPAddress guestIP);

        public Session<R,S,T> Close(string sessionId, IPAddress hostIP);

        public Session<R,S,T> GetHostData(string sessionId, IPAddress hostIP);

        public Session<R,S,T> SetHostData(string sessionId, IPAddress hostIP, Session<R,S,T> session);

        public UserData<S, T> GetUserData(string sessionId, IPAddress guestIP);

        public UserData<S, T> SetUserData(string sessionId, IPAddress guestIP, UserData<S,T> userData);
    }
}
