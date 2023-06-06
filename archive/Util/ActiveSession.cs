using facetious_nocturn.Models;

namespace facetious_nocturn.Util
{
    public class ActiveSession<R,S,T>
    {
        public Session<R,S,T> Session { get; }

        private Queue<Action<Session<R, S, T>>> _changeRequestQueue;

        private bool _isActive;

        public ActiveSession(Session<R, S, T>? session)
        {
            Session = ((session != null) ? session : new Session<R,S, T>());
            _changeRequestQueue = new Queue<Action<Session<R, S, T>>>();
            _isActive = true;
            Thread t = new Thread(() => {
                while(_isActive)
                {
                    while(_changeRequestQueue.Any())
                    {
                        _changeRequestQueue.Dequeue().Invoke(Session);
                    }
                }
            });
        }

        public UserData<S,T> GetUserData(string guestKey)
        {
            return new UserData<S, T>
            {
                SessionId = Session.Id,
                Context = Session.Context,
                Guest = Session.Guests[guestKey]
            };
        }

        public void SubmitRequest(Action<Session<R, S, T>> request)
        {
            _changeRequestQueue.Enqueue(request);
        }

        public void Close()
        {
            _isActive = false;
        }

        public void Wait()
        {
            while (_changeRequestQueue.Any()) { }
        }
    }
}
