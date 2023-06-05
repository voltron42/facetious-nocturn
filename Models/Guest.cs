namespace facetious_nocturn.Models
{
    public class Guest<T> : Host<T>
    {
        public long Joined { get; set; }
        public long? Exited { get; set; }
    }
}
