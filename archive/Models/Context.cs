namespace facetious_nocturn.Models
{
    public class Context<T>
    {
        public long LastUpdated { get; set; }
        public T State { get; set; }
    }
}
