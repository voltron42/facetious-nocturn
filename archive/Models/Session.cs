namespace facetious_nocturn.Models
{
    public class Session<R,S,T>
    {
        public string Id { get; set; }
        public string Key { get; set; }
        public Host<R> Host { get; set; }
        public Context<S> Context { get; set; }
        public Dictionary<string, Guest<T>> Guests { get; }

        public Session()
        {
            Guests = new Dictionary<string,Guest<T>>();
        }
    }
}
