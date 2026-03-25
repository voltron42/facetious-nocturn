namespace("spyfall.SpyfallGuest", {
  "facetious-nocturn.Guest": "Guest",
  "facetious-nocturn.Session": "Session",
  "spyfall.Discussion": "Discussion",
  "spyfall.Voting": "Voting",
  "spyfall.Results": "Results"
}, ({ Guest, Session, Discussion, Voting, Results }) => {
  const confirmIcon = (guestId,state) => {
    if (state == guestId) {
      return <i className="fas fa-check-circle text-success"></i>;
    } else if (state) {
      return <i className="fas fa-circle-xmark text-warning"></i>;
    } else {
      return <></>;
    }
  };
  return class extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
        screen: "invite",
        sessionId: props.sessionId
      }
    }
    loadSessionToConfirm() {
      Session.getCommonData(this.state.sessionId, ({ commonData }) => {
        this.setState({
          screen: "confirm",
          hostName: commonData.hostName,
          playerNames: Object.entries(commonData.playerNames).reduce((acc, [name, state]) => {
            acc[name] = state;
            return acc;
          }, {})
        });
      }, () => this.error());
    }
    endDiscussionPoll(commonData) {
      if (commonData.countdown.expired) {
        this.state.endDiscussionPoll();
        this.setState({
          screen: "voting",
          endDiscussionPoll: undefined,
          endVotingPoll: this.state.guest.poll(({ commonData }) => {
            const remainingVotes = Object.values(commonData.votes).filter(vote => !vote).length;
            if (remainingVotes == 0) {
              this.state.endVotingPoll();
              this.setState({
                screen: "results",
                history: commonData.history,
                endVotingPoll: undefined,
                endResultsPoll: this.state.guest.poll(({ commonData }) => this.endResultsPoll(commonData), () => this.error())
              });
            } else {
              this.setState({
                votes: commonData.votes,
              });
            }
          }, () => this.error())
        });
      } else if (commonData.chosenLocation) {
        this.state.endDiscussionPoll();
        this.setState({
          screen: "results",
          history: commonData.history,
          endDiscussionPoll: undefined,
          endResultsPoll: this.state.guest.poll(({ commonData }) => this.endResultsPoll(commonData), () => this.error())
        });
      } else {
        this.setState({
          countdown: commonData.countdown,
          chosenLocation: commonData.chosenLocation,
        });
      }
    }
    endResultsPoll(commonData) {
      const newGameConfirmCount = Object.values(commonData.newGameConfirms).filter(([_id, state]) => state).length;
      if (newGameConfirmCount == 0) {
        this.state.endResultsPoll();
        this.setState({
          screen: "discussion",
          countdown: commonData.countdown,
          location: commonData.location,
          role: commonData.roles[guestId],
          locations: Array.from(commonData.locations),
          endResultsPoll: undefined,
          newGameConfirms: undefined,
          endDiscussionPoll: this.state.guest.poll(({ commonData }) => this.endDiscussionPoll(commonData), () => this.error())
        });
      } else {
        this.setState({
          newGameConfirms: commonData.newGameConfirms
        });
      }
    }
    enterLobby() {
      Guest.joinSession(this.state.sessionId, {}, ({ guestId }) => {
        const guest = new Guest(this.state.sessionId, guestId);
        this.setState({
          screen: "lobby",
          guestId: guestId,
          guest: guest,
          endLobbyPoll: guest.poll(({ commonData }) => {
            const playerNames = Object.entries(commonData.playerNames).reduce((acc, [name, state]) => {
              acc[name] = state;
              return acc;
            }, {});
            if (this.state.name) {
              playerNames[this.state.name] = this.state.guestId;
            }
            const availableCount = Object.values(playerNames).filter(state => !state).length;
            if (availableCount == 0) {
              this.state.endLobbyPoll();
              this.setState({
                screen: "discussion",
                countdown: commonData.countdown,
                location: commonData.location,
                role: commonData.roles[guestId],
                locations: Array.from(commonData.locations),
                endLobbyPoll: undefined,
                endDiscussionPoll: this.state.guest.poll(({ commonData }) => this.endDiscussionPoll(commonData), () => this.error())
              });
            } else {
              this.setState({ playerNames });
            }
          }, () => this.error())
        });
      });
    }
    selectPlayerName(name) {
      this.state.guest.update({ guestData: { name }}, () => {
        // update queued, do nothing until next poll
      }, () => this.error());
      const playerNames = Object.entries(this.state.playerNames).reduce((acc, [playerName, state]) => {
        acc[playerName] = state;
        return acc;
      }, {});
      playerNames[name] = this.state.guestId;
      this.setState({ playerNames, name });
    }
    selectLocation(location) {
      this.state.guest.update({ guestData:{ name: this.state.name, chosenLocation: location }}, () => {
        // update queued, do nothing until next poll
      }, () => this.error());
      this.setState({ chosenLocation: location, myChosenLocation: location });
    }
    vote(name) {
      this.state.guest.update({ guestData: { name: this.state.name, vote: name }}, () => {
        // update queued, do nothing until next poll
      }, () => this.error());
      this.setState({ vote: name });
    }
    playAgain() {
      this.state.guest.update({ guestData: { name: this.state.name, newGameConfirm: true }}, () => {
        // update queued, do nothing until next poll
      }, () => this.error());
      this.setState({ newGameConfirm: true });
    }
    quit() {
      this.state.guest.quit(() => this.setState({ screen: "quit" }));
      this.setState({ screen: "quit" });
    }
    error() {
      this.setState({ screen: undefined });
    }
    render() {
      switch (this.state.screen) {
        case "invite":
          return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
            <h1 className="mb-4">You've been invited to join a game of Spyfall!</h1>
            <button className="btn btn-primary" onClick={() => this.loadSessionToConfirm()}>Join Game</button>
          </div>);
        case "confirm":
          return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
            <h1 className="mb-4">Confirm Game To Join</h1>
            <h3 className="mb-4">Host: {this.state.hostName}</h3>
            <h3 className="mb-4">Current Players:</h3>
            <ul className="mb-4">
              {Object.entries(this.state.playerNames).map(([name, state]) => <li key={name}>{name}{confirmIcon(this.state.guestId, state)}</li>) }
            </ul>
            <button className="btn btn-primary" onClick={() => this.enterLobby()}>Join Game</button>
          </div>);
        case "lobby":
          return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
            <h1 className="mb-4">Select Your Name To Confirm Invite</h1>
            <h3 className="mb-4">Host: {this.state.hostName}</h3>
            <h3 className="mb-4">Current Players:</h3>
            <ul className="mb-4">
              {Object.entries(this.state.playerNames).map(([name, state]) => {
                if (state) {
                  return <li key={name}>{name}{confirmIcon(state)}</li>
                } else {
                  return <li key={name}><button className="btn btn-outline-primary" onClick={() => this.selectPlayerName(name)}>{name}</button></li>;
                }
              }) }
            </ul>
          </div>);
        case "discussion":
          return <Discussion role={this.state.role} location={this.state.location} locations={this.state.locations} onSelectLocation={(location) => this.selectLocation(location)} />;
        case "voting":
          return <Voting playerNames={this.state.playerNames} vote={this.state.vote} onVote={(name) => this.vote(name)} />;
        case "results":
          return <Results history={this.state.history}>
            <button className="btn btn-primary" onClick={() => this.playAgain()}>Play Again</button>
            <button className="btn btn-secondary ms-2" onClick={() => this.quit()}>Quit</button>
          </Results>;
        case "quit":
          return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
            <h1 className="mb-4">You've left the game</h1>
          </div>);
        default: // error screen
          return (<div className="d-flex flex-column align-items-center justify-content-center h-100">
            <h1 className="mb-4">An error has occurred</h1>
          </div>);
      }
    }
  }
});