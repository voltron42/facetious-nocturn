namespace("spyfall.SpyfallHost", {
  "facetious-nocturn.Callbacks": "Callbacks",
  "facetious-nocturn.Host": "Host",
  "spyfall.Discussion": "Discussion",
  "spyfall.Locations": "Locations",
  "spyfall.Voting": "Voting",
  "spyfall.Results": "Results"
}, ({ Callbacks, Host, Discussion, Locations, Voting, Results }) => {
  const confirmIcon = (state) => {
    if (state) {
      return <i className="fas fa-check-circle text-success"></i>;
    } else {
      return <></>;
    }
  };
  return class extends React.Component {
    constructor(props) {
      super(props);
      const { sessionId, hostId } = props;
      this.state = {
        host: Host.init(sessionId, hostId),
        screen: "init",
        hostName: "",
        guestNames: [""],
      };
    }
    isLobbyReady() {
      return this.state.hostName.trim().length > 0 && 
        this.state.guestNames.length > 0 && 
        this.state.guestNames.reduce((acc, name) => acc && name.trim().length > 0, true) &&
        this.state.guestNames.filter((name, index) => this.state.guestNames.map(n => n.trim()).indexOf(name) != index).length == 0 &&
        this.state.guestNames.map(n => n.trim()).indexOf(this.state.hostName.trim()) == -1;
    }
    lockInLobby() {
      const guestNames = this.state.guestNames.reduce((acc, name) => {
        acc[name] = false;
        return acc;
      }, {});
      this.state.host.update({ 
        hostData: { name: this.state.hostName },
        commonData: { guestNames }
      });
      this.setState({
        screen: "lobby",
        guestNames,
        endLobbyPoll: this.state.host.poll(({ commonData, guests }) => {
          console.log({ commonData, guests });
          if (commonData.guestNames) {
            var changed = false;
            Object.entries(guests).forEach(([id, guest]) => {
              const currentNames = Object.entries(commonData.guestNames).filter(([_name, _id]) => _id == id && _name != guest.name).map(([name]) => name);
              for(var i = 0; i < currentNames.length; i++) {
                delete commonData.guestNames[guest.name];
                changed = true;
              }
              if (guest.name) {
                commonData.guestNames[guest.name] = id;
                changed = true;
              }
            });
            const usedIds = Object.values(commonData.guestNames);
            const ids = Object.keys(guests);
            if (changed) {
              const completed = ids.filter(id => usedIds.indexOf(id) < 0).length > 0;
              this.state.host.update({ commonData }, () => {
                // update queued, do nothing until next poll
              }, (error) => this.error(error));
              this.setState({ guestNames });
            }
          }
        }, (error) => this.error(error))
      });
    }
    selectLocation(location) {
      this.state.host.update({ hostData: { name: this.state.hostName, chosenLocation: location } }, () => {
        // update queued, do nothing until next poll
      }, (error) => this.error(error));
    }
    vote(name) {
      this.state.host.update({ hostData: { name: this.state.hostName, vote: name } }, () => {
        // update queued, do nothing until next poll
      }, (error) => this.error(error));
    }
    playAgain() {
      this.state.host.update({ hostData: { name: this.state.hostName, newGameConfirm: true } }, () => {
        // update queued, do nothing until next poll
      }, (error) => this.error(error));
    }
    kickGuest(name, guestId) {
      const playerNames = { ...this.state.playerNames };
      delete playerNames[name];
      this.state.host.update({ playerNames }, () => {
        // update queued, do nothing until next poll
      }, (error) => this.error(error));
      this.state.host.kick(guestId, () => {
        // update queued, do nothing until next poll
      }, (error) => this.error(error));
      this.setState({ playerNames });
    }
    quit() {
      this.state.host.close(() => this.setState({ screen: "quit" }));
      this.setState({ screen: "quit" });
    }
    error(error) {
      Callbacks.defaultOnError(error);
      this.setState({ screen: undefined });
    }
    render() {
      switch (this.state.screen) {
        case "init":
          return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
            <h1>Hosting Spyfall</h1>
            <p>Enter your name and the names of your players</p>
            <div className="form-group m-3">
              <label htmlFor="hostName">Your Name</label>
              <input type="text" className="form-control" id="hostName" placeholder="Enter your name" value={this.state.hostName} onChange={(e) => this.setState({ hostName: e.target.value })}/>
            </div>
            { this.state.guestNames.map((name, index) => (
              <div className="form-group m-3" key={index}>
                <label htmlFor={`guestName${index}`}>Player {index + 1} Name</label>
                <input type="text" className="form-control" id={`guestName${index}`} placeholder={`Enter player ${index + 1} name`} value={name} onChange={(e) => {
                  const newGuestNames = [...this.state.guestNames];
                  newGuestNames[index] = e.target.value;
                  this.setState({ guestNames: newGuestNames });
                }}/>
                { this.state.guestNames.map(n => n.trim()).indexOf(name.trim()) != index && <small className="form-text text-danger">Player names must be unique</small> }
                { name.trim() === this.state.hostName.trim() && <small className="form-text text-danger">Player name cannot be the same as host name</small> }
              </div>
            ))}
            <div>
              <button className="btn btn-primary m-3" onClick={() => this.setState({ guestNames: [...this.state.guestNames, ""] })}>Add Player</button>
            </div>
            <div>
              { this.isLobbyReady() && <button className="btn btn-success m-3" onClick={() => this.lockInLobby()}>Lock In Lobby</button> }
            </div>
          </div>);
        case "lobby":
          return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
            <h1>Lobby</h1>
            <p>Waiting for players to join...</p>
            <h3 className="mb-4">Host: {this.state.hostName}</h3>
            <h3 className="mb-4">Current Players:</h3>
            <ul className="mb-4">
              {Object.entries(this.state.guestNames).map(([name, state]) => <li key={name}>{name}{confirmIcon(state)}</li>) }
            </ul>
            <div>
              <button className="btn btn-success" onClick={() => this.state.host.copyInvite()}>Click to copy link for invite</button>
            </div>
          </div>);
        case "discussion":
          return (<Discussion  role={this.state.role} location={this.state.location} locations={this.state.locations} onSelectLocation={(location) => this.selectLocation(location)}/>);
        case "voting":
          return <Voting playerNames={this.state.playerNames} vote={this.state.vote} onVote={(name) => this.vote(name)} />;
        case "results":
          return <Results history={this.state.history}>
            <button className="btn btn-primary" onClick={() => this.playAgain()}>Play Again</button>
            { /* todo - Host can also kick players from results screen */ }
            <ul className="mb-4">
              {Object.entries(this.state.playerNames).map(([name, guestId]) => <li key={name}>{name}<a href="#" onClick={(e) => {
                e.preventDefault();
                this.kickGuest(name, guestId);
              }}><i className="fas fa-circle-xmark text-danger"></i></a></li>) }
            </ul>
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