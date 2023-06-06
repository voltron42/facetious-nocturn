namespace facetious_nocturn.Models
{
    public class Host<T> : Context<T>
    {
        public string Key { get; set; }
        public string NameTag { get; set; }
    }
}
