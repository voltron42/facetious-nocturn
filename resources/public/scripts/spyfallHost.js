namespace("spyfall.SpyfallHost", {
  "facetious-nocturn.Host": "Host",
  "spyfall.Discussion": "Discussion",
  "spyfall.Voting": "Voting",
  "spyfall.Results": "Results"
}, ({ Host, Discussion, Voting, Results }) => {
  return class extends React.Component {
    constructor({ sessionId, hostId }) {
      super({ sessionId, hostId });
      this.state = {
        host: new Host(sessionId, hostId),
        screen: "init",
      };
    }
    lockInLobby() {
      const errors = [];
      // todo - validate lobby state before locking in
      if (errors.length == 0) {
        // todo - update host state to lock in lobby, start polling for guests to confirm lobby state
        this.setState({
          screen: "lobby",
          lobbyErrors: undefined,
          endLobbyPoll: host.poll(({ commonData }) => {
            // todo - update commonData from guest data, update lobby screen from commonData
          }, () => this.error())
        });
      } else {
        // todo - display errors to host
        this.setState({ lobbyErrors: errors });
      }
    }
    selectLocation(location) {
      this.state.host.update({ chosenLocation: location }, () => {
        // update queued, do nothing until next poll
      }, () => this.error());
    }
    playAgain() {
      this.state.host.update({ newGameConfirm: true }, () => {
        // update queued, do nothing until next poll
      }, () => this.error());
    }
    quit() {
      this.state.host.leave(() => this.setState({ screen: "quit" }));
      this.setState({ screen: "quit" });
    }
    error() {
      this.setState({ screen: undefined });
    }
    render() {
      switch (this.state.screen) {
        case "init":
          return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
            { /* todo - Init content */ }
          </div>);
        case "lobby":
          return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
            { /* todo - Lobby content */ }
          </div>);
        case "discussion":
          return (<Discussion  role={this.state.role} location={this.state.location} locations={this.state.locations} onSelectLocation={(location) => this.selectLocation(location)}/>);
        case "voting":
          return <Voting playerNames={this.state.playerNames} vote={this.state.vote} onVote={(name) => this.vote(name)} />;
        case "results":
          return <Results history={this.state.history}>
            <button className="btn btn-primary" onClick={() => this.playAgain()}>Play Again</button>
            { /* todo - Host can also kick players from results screen */ }
            <button className="btn btn-secondary ms-2" onClick={() => this.quit()}>Quit</button>
          </Results>;
        default: // error screen
          return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
            <h1 className="mb-4">An error has occurred</h1>
          </div>);
      }
    }
  }
});