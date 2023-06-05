namespace facetious_nocturn.Models
{
    public class UserData<S,T>
    {
        public Context<S> Context { get; set; }
        public Guest<T> Guest { get; set; }
    }
}
