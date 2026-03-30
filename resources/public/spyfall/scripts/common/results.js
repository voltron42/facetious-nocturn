namespace("spyfall.common.Results", {}, () => {
  return function(props) {
    const latestGame = props.history[props.history.length - 1];
    const voteCounts = Object.entries(latestGame.voteCounts);
    voteCounts.sort(([$, countA], [$$, countB]) => countB - countA);
    return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
      <h1 className="mb-4">Game Results</h1>
      <h1 className="mb-4">{latestGame.result}</h1>
      <h3 className="mb-4">Location: {latestGame.location}</h3>
      <h3 className="mb-4">Spy: {latestGame.spyName}</h3>
      <h3 className="mb-4">Votes:</h3>
      <ul className="mb-4">
        {voteCounts.map(([name, count]) => <li key={name}>{name} ({latestGame.roles[name]}): {count}</li>)}
      </ul>
      <div>
        {props.children}
      </div>
      <h3 className="mt-4">Scoreboard</h3>
      <table className="table table-striped">
        <thead>
          <tr>
            <td>Player</td>
            <td>Wins</td>
            <td>Losses</td>
          </tr>
        </thead>
        <tbody>
          {Object.entries(props.history.reduce((acc, game) => {
            const spyName = game.spyName;
            if (!acc[spyName]) acc[spyName] = { wins: 0, losses: 0 };
            if (game.result === "Spy Wins") {
              acc[spyName].wins++;
            } else {
              acc[spyName].losses++;
            }
            return acc;
          }, {})).filter(([_, { wins, losses }]) => wins > 0 || losses > 0).map(([name, { wins, losses }]) => (
            <tr key={name}>
              <td>{name}</td>
              <td>{wins}</td>
              <td>{losses}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <h3 className="mt-4">History</h3>
      <table className="table table-striped">
        <thead>
          <tr>
            <td>Result</td>
            <td>Location</td>
            <td>Spy</td>
            <td>Votes In Favor</td>
            <td>Votes Against</td>
          </tr>
        </thead>
        <tbody>
          {props.history.map((game, index) => {
            const voteCounts = Object.entries(game.voteCounts);
            const inFavorCount = voteCounts[game.spyName];
            const againstCount = Object.values(game.voteCounts).reduce((acc, count) => acc + count, 0) - inFavorCount;
            return <tr key={index}>
              <td>{game.result}</td>
              <td>{game.location}</td>
              <td>{game.spyName}</td>
              <td>{inFavorCount}</td>
              <td>{againstCount}</td>
            </tr>;
          })}
        </tbody>
      </table>
    </div>);

  };
});