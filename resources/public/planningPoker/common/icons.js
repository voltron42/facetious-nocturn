namespace("planning-poker.common.Icons", {}, () => {
  const iconMap = {
    "?": "fa-circle-question",
    "1": "fa-1",
    "2": "fa-2",
    "3": "fa-3",
    "5": "fa-5",
    "8": "fa-8",
  };
  return {
    get: function(icon) {
      return <i className={`fas ${iconMap[icon] || "fa-square-full"}`}></i>;
    },
    getAll: function() {
      return Object.keys(iconMap);
    }
  };
});