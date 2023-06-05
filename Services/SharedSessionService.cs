using facetious_nocturn.Models;
using facetious_nocturn.Util;
using System.Net;

namespace facetious_nocturn.Services
{
    public class SharedSessionService<R, S, T> : ISharedSessionService<R, S, T>
    {
        private Dictionary<string, ActiveSession<R,S,T>> sessionCache;
        private Dictionary<string, string> sessionKeyMap;
        public SharedSessionService() {
            sessionCache = new Dictionary<string,ActiveSession<R,S,T>>();
            sessionKeyMap = new Dictionary<string, string>();
        }

        public Session<R, S, T> Host(IPAddress ipAddress, string nameTag, R hostState, S context)
        {
            string sessionId = BuildSessionId(ipAddress);
            string sessionKey = BuildSessionKey(ipAddress);
            long lastUpdated = GetLastUpdated();
            Session<R, S, T> newSession = new Session<R, S, T>
            {
                Id = sessionId,
                Key = sessionKey,
                Host = new Host<R>
                {
                    NameTag = nameTag,
                    Key = BuildGuestKey(ipAddress),
                    State = hostState,
                    LastUpdated = lastUpdated,
                },
                Context = new Context<S>
                {
                    State = context,
                    LastUpdated = lastUpdated
                }
            };
            sessionCache.Add(sessionId, new ActiveSession<R, S, T>(newSession));
            sessionKeyMap.Add(sessionKey, sessionId);
            return newSession;
        }

        public Guest<T> Join(string sessionKey, IPAddress ipAddress, string nameTag, T guestState)
        {
            string sessionId = GetSessionId(sessionKey);
            ActiveSession<R, S, T> session = GetSession(sessionId);
            long lastUpdated = GetLastUpdated();
            string guestKey = BuildGuestKey(ipAddress);
            Guest<T> newGuest = new Guest<T>
            {
                Key = guestKey,
                NameTag = nameTag,
                LastUpdated = lastUpdated,
                Joined = lastUpdated,
                State = guestState,
            };
            session.SubmitRequest((Session<R,S,T> session) =>
            {
                session.Guests[guestKey] = newGuest;
            });
            return newGuest;
        }

        public Guest<T> Kick(string sessionId, IPAddress hostIP, string guestKey)
        {
            ActiveSession<R, S, T> activeSession = GetSession(sessionId);
            ValidateHostIP(activeSession, hostIP);
            Guest<T> guest = GetGuest(activeSession, guestKey);
            activeSession.SubmitRequest((Session<R, S, T> session) =>
            {
                session.Guests.Remove(guestKey);
            });
            return guest;
        }

        public Guest<T> Leave(string sessionId, IPAddress guestIP)
        {
            ActiveSession<R, S, T> activeSession = GetSession(sessionId);
            string guestKey = BuildGuestKey(guestIP);
            Guest<T> guest = GetGuest(activeSession, guestKey);
            activeSession.SubmitRequest((Session<R, S, T> session) =>
            {
                session.Guests.Remove(guestKey);
            });
            return guest;
        }

        public Session<R, S, T> Close(string sessionId, IPAddress hostIP)
        {
            ActiveSession<R, S, T> session = GetSession(sessionId);
            session.Close();
            session.Wait();
            sessionCache.Remove(sessionId);
            sessionKeyMap.Remove(session.Session.Key);
            return session.Session;
        }

        public Session<R, S, T> GetHostData(string sessionId, IPAddress hostIP)
        {
            ActiveSession<R, S, T> session = GetSession(sessionId);
            ValidateHostIP(session, hostIP);
            return session.Session;
        }

        public UserData<S, T> GetUserData(string sessionId, IPAddress guestIP)
        {
            ActiveSession<R, S, T> session = GetSession(sessionId);
            string guestKey = BuildGuestKey(guestIP);
            Guest<T> guest = GetGuest(session, guestKey);
            return new UserData<S, T>
            {
                Guest = guest,
                Context = session.Session.Context
            };
        }

        public Session<R, S, T> SetHostData(string sessionId, IPAddress hostIP, Session<R, S, T> newState)
        {
            ActiveSession<R, S, T> activeSession = GetSession(sessionId);
            ValidateHostIP(activeSession, hostIP);
            activeSession.SubmitRequest((Session<R, S, T> oldState) =>
            {
                long lastUpdated = GetLastUpdated();
                // todo - update host
                // todo - update each guest
                oldState.Context.LastUpdated = lastUpdated;
                oldState.Context.State = newState.Context.State;
            });
            return activeSession.Session;
        }

        public UserData<S, T> SetUserData(string sessionId, IPAddress guestIP, UserData<S, T> userData)
        {
            ActiveSession<R, S, T> activeSession = GetSession(sessionId);
            string guestKey = BuildGuestKey(guestIP);
            Guest<T> guest = GetGuest(activeSession, guestKey);
            activeSession.SubmitRequest((Session<R, S, T> session) =>
            {
                long lastUpdated = GetLastUpdated();
                Guest<T> guest = session.Guests[guestKey];
                if (guest != null)
                {
                    guest.LastUpdated = lastUpdated;
                    guest.NameTag = userData.Guest.NameTag;
                    guest.State = userData.Guest.State;
                }
                session.Context.LastUpdated = lastUpdated;
                session.Context.State = userData.Context.State;
            });
            return new UserData<S, T>
            {
                Guest = guest,
                Context = activeSession.Session.Context
            };
        }

        private string BuildSessionId(IPAddress ipAddress)
        {
            // todo
            return ipAddress.ToString();
        }

        private string BuildSessionKey(IPAddress ipAddress)
        {
            // todo
            return ipAddress.ToString();
        }

        private string BuildGuestKey(IPAddress ipAddress)
        {
            // todo
            return ipAddress.ToString();
        }

        private long GetLastUpdated()
        {
            return DateTime.Now.ToFileTimeUtc();
        }

        private ActiveSession<R, S, T> GetSession(string sessionId)
        {
            ActiveSession<R, S, T> session = sessionCache[sessionId];
            if (session == null)
            {
                throw new KeyNotFoundException(sessionId);
            }
            return session;
        }

        private string GetSessionId(string sessionKey)
        {
            string sessionId = sessionKeyMap[sessionKey];
            if (sessionId == null)
            {
                throw new KeyNotFoundException(sessionKey);
            }
            return sessionId;
        }

        private void ValidateHostIP(ActiveSession<R, S, T> session, IPAddress hostIP)
        {
            string hostKey = BuildGuestKey(hostIP);
            if (session.Session.Host.Key != hostKey)
            {
                throw new BadHttpRequestException(hostKey);
            }
        }

        private Guest<T> GetGuest(ActiveSession<R, S, T> session, string guestIP)
        {
            Guest<T> guest = session.Session.Guests[guestIP];
            if (guest == null)
            {
                throw new BadHttpRequestException(guestIP);
            }
            return guest;
        }
    }
}
