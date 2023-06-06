namespace facetious_nocturn.Models
{
    public class UserData<S,T>
    {
        public string SessionId { get; set; }

        public Context<S> Context { get; set; }
        public Guest<T> Guest { get; set; }
    }
}
