namespace("facetious-nocturn.Diff", {}, () => {
  const deepRecursive = function(path, previous, current) {
    const prevType = (typeof previous);
    const currType = (typeof current);
    if (prevType != currType) {
      return [{
        path: Array.from(path),
        previous: { typeof: prevType},
        current: { typeof: current },
      }]
    } else if (prevType != "object") {
      if (previous == current) {
        return []
      } else {
        return [{
          path: Array.from(path),
          previous,
          current,
        }];
      }
    } else {
      const isPrevArray = Array.isArray(previous);
      const isCurrArray = Array.isArray(current);
      if (isPrevArray != isCurrArray) {
        return [{
          path: Array.from(path),
          previous: { isArray: isPrevArray },
          current: { isArray: isCurrArray }
        }];
      } else if (isPrevArray) {
        // diff array
        if (previous.length != current.length) {
          return [{
            previous: { length: previous.length },
            current: { length: current.length }
          }]
        } else {
          return previous.reduce((acc, prevItem, i) => {
            const currItem = current[i];
            return acc.concat(deepRecursive(path.concat([i]), prevItem, currItem));
          }, []);
        }
      } else {
        const prevKeys = Object.keys(previous);
        const currKeys = Object.keys(current);
        const prevExclusive = prevKeys.filter(k => !(k in current));
        const currExclusive = currKeys.filter(k => !(k in previous));
        
        if (prevExclusive.length > 0 || currExclusive.length > 0) {
          return [{
            path: Array.from(path),
            previous: { keys: prevExclusive },
            current: { keys: currExclusive }
          }];
        } else {
          return prevKeys.reduce((acc, key) => {
            const prevVal = previous[key];
            const currVal = current[key];
            return acc.concat(deepRecursive(path.concat([key]), prevVal, currVal));
          }, []);
        }
      }
    }
  }
  const deep = function(previous, current) {
    return deepRecursive([], previous, current);
  }
  return { deep };
});