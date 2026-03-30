# Spyfall

## App Flow

1. Host new game
   * Host initializes a game, enters their name, and lists the guest names (all names must be unique)
   * An 'Invite' link is produced for the host to copy to the clipboard to send out
2. Guests click the 'Invite' link
   * The 'Invite' page gives the guests the ability to select which guest they are from the list of names
   * Once all the 'guests' have 'arrived', the game can begin
3. The Host initializes the game
   * The 'Host' and all the 'Guests' become 'Players'
   * a 'Location' is selected at random
   * one of the 'Players' is chosen at random to be the spy
   * the rest of the 'Players' are each assigned a different 'Role' from the 'Location'
4. All Players UIs update for "Discussion"
   * any player assigned a role will see both the 'Location' and their 'Role' on the screen
   * the spy will **Not** see the 'Location' or a 'Role', but will instead see the message "You are The Spy!" and a list of possible locations to select from.
     * If the spy selects one of the locations, the game ends, and the spy wins if (and only if) they guess correctly
   * All players will also see a timer ticking down in minutes and seconds from a configurable time value.
5. All Players UIs update for "Voting"
   * If the Spy has not guessed the location before the timer has run out, the players (spy included) will be presented a list of player names to select from so that they may vote for who they think is the spy.
   * if they vote correctly, the Spy loses. If they vote for anyone other than the spy, the spy wins.
   * During the session, the location, the identity of the spy, and the outcome ("Spy Wins", "Spy Loses"), are tracked as "History".
6. Playing again:
   * The app will never choose the same player as the spy consecutively
   * Until no less than half the locations have been played, the game will not pick the same location again.
     * after at least half of the locations have been played, the locations will be reshuffled, discarding the last location played

## Screen List

1. Host Lobby Screen
   * input for 'Host' name
   * 'Add Guest' button
   * Each click of 'Add Guest' adds a text input to a bulleted list
     * No more than 7 guests are allowed
     * No two names can be the same
   * 'Close List' makes the 'Add Guest' button disappear and the 'Invite' button appear.
   * 'Invite' copies the invite link to the clipboard and alerts the 'Host' that this has been done.
   * As the 'Guests' accept their invites and choose their names, an icon will appear next to their names to indicate that they have done so.
   * Once all the 'Guests' have chosen their names, a 'Start Game' button will appear
     * 'Start Game' will perform the actions listed under #3 in the App Flow
2. Guest Invite Screen
   * when the 'Guest' uses the invite link to arrive in the app, they will see a list of 'Guest' names to choose from
   * once they choose, an icon will appear next to their chosen name indicating their choice, and all options disabled.
   * As the other 'Guests' choose their names, other icons will appear showing that those names have been chosen as well.
3. Spy Discussion Screen
   * timer at the top counting down in minutes and seconds
   * "You Are The Spy!"
   * A list of locations to select from
4. Role Discussion Screen
   * timer at the top counting down in minutes and seconds
   * Location and Role
   * a static list of other locations 
5. Voting Screen
   * timer at the top counting down in minutes and seconds
   * a list of players to choose from
     * 
6. Endgame screen
   * screen will display either "Spy Wins!" or "Spy Loses!"
   * host will be presented with a "Play Again?"
7. History screen
   * display a table of all games played for this session
   * columns are "Location", "Spy", "Result"
     * "Result" will be either "Spy Wins!" or "Spy Loses!"
8. Menu Options
   * a menu button will provide all players access to the history screen
   * the menu will include a 'Quit' option for 'Guests'
   * the menu will include a 'Kick' option for each player, and a 'Close Session' option.