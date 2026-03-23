namespace("facetious-nocturn.Session", {
  "gizmo-atheneum.namespaces.Ajax": "Ajax",
  "facetious-nocturn.Callbacks": "Callbacks",
  "facetious-nocturn.Constants": "Constants"
}, ({ Ajax, Callbacks, Constants }) => {
  const getCommonData = function(sessionId, onSuccess, onError) {
    Ajax.get(`${Constants.API_BASE}/sessions/${sessionId}/common`, Callbacks(onSuccess, onError));
  };
  return { getCommonData };
})