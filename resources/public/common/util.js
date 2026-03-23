namespace("common.Utilities", {}, () => {
  const getUrlQuery = () => {
    return Array.from(new URLSearchParams(window.location.search).entries()).reduce((acc, [key, value]) => {
      if (Array.isArray(acc[key])) {
        acc[key].push(value);
      } else if (acc[key]){
        acc[key] = [acc[key], value];
      } else {
        acc[key] = value;
      }
      return acc;
    }, {});
  };
  const appendQueryToUrl = (query) => {
    const url = location.href.split("?")[0];
    const queryString = Object.entries(query).map(([key, value]) => `${key}=${value}`).join("&");
    location.assign(`${url}?${queryString}`);
  };
  return { getUrlQuery, appendQueryToUrl };
});