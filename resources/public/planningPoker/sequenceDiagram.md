# Planning Poker Sequence Diagram

## Overview
This diagram shows the interaction flow for the Planning Poker application using the Facetious Nocturn session system.

```mermaid
sequenceDiagram
    participant HU as Host User
    participant RP as Root Page
    participant HA as Host App
    participant S as Server
    participant GA as Guest App
    participant GU as Guest User

    HU->>RP: Trigger new session
    RP->>S: Create session
    S->>RP: Return session-id & host-id
    RP->>HA: Route to Host app
    
    HU->>HA: Enter host name & guest names
    HA->>S: Submit names (guestNames: {name: false})
    S->>HA: Confirm update
    HA->>HA: Switch to Lobby view
    HA->>HA: Begin polling for changes
    
    HU->>HA: Click "Copy guest invite link" button
    HA->>HU: guest invite link is copied to clipboard
    HU->>GU: Send link (via external means)
    
    GU->>GA: Click link with sessionId
    GA->>S: GET session (common data)
    S->>GA: Return host name & guest names
    GA->>GA: Display Confirm Invite screen
    
    GU->>GA: Click "Confirm Invite"
    GA->>S: Send join request
    S->>GA: Return guest-id
    GA->>GA: Switch to Lobby view
    GA->>GA: Begin polling for commonData
    
    GU->>GA: Click name to confirm identity
    GA->>S: Update with confirmed name
    S->>HA: Poll returns updated guest name
    
    HA->>S: Update guestNames (name: true)
    S->>GA: Poll returns updated guestNames
    
    GA->>GA: Show confirm icon for accepted name
    HA->>HA: Check if all guests confirmed
    
    Note over HA,GA: All guests confirmed - Transition to Planning Poker
    
    HA->>S: Update with votes map (all undefined)
    S->>GA: Poll returns votes map
    HA->>HA: Switch to Planning Poker view
    GA->>GA: Switch to Planning Poker view
    
    HU->>HA: Click vote icon
    HA->>S: Update hostData with vote
    S->>HA: Confirm update
    HA->>HA: Hide buttons, show own vote
    
    GU->>GA: Click vote icon
    GA->>S: Update fromGuest with vote
    S->>GA: Confirm update
    GA->>GA: Hide buttons, show own vote
    
    HA->>HA: Poll checks if all guests voted
    HA->>S: Update votes map with all votes
    S->>HA: Confirm update
    HA->>HA: Display "Clear for next vote" button
    
    GA->>GA: Poll receives updated votes map
    GA->>GA: Display all votes
    GA->>GA: Begin polling for clearing property
    
    HU->>HA: Click "Clear for next vote"
    HA->>S: Update: remove hostData vote, reset votes, set clearing: true
    S->>GA: Poll returns clearing: true
    
    GA->>S: Update: remove vote from fromGuest
    S->>GA: Confirm update
    GA->>GA: Begin polling for clearing to be removed
    
    HA->>HA: Poll checks if all guests cleared
    HA->>S: Update: remove clearing property
    S->>GA: Poll returns clearing removed
    
    GA->>GA: Begin polling for new votes
    HA->>HA: Ready for next vote round
    
    Note over HU,GU: Cycle repeats for next vote
```

## Legend

| Actor | Role |
|-------|------|
| Host User | Initiates session and manages voting rounds |
| Root Page | Entry point that routes to Host app |
| Host App | Manages session, coordinates guest confirmations, and aggregates votes |
| Server | Persists session state and serves as communication hub |
| Guest App | Displays voting interface for guest users |
| Guest User | Accepts invitation and submits votes |

## Key Data Structures

### Common Data (shared between Host and Guests)
- `hostName`: Name of the host
- `guestNames`: Map of guest names with boolean status (false = not confirmed, true = confirmed)
- `votes`: Map of votes from all participants (initially undefined values)
- `clearing`: Boolean flag to signal vote clearing (removed once complete)

### From Guest Data
- `name`: Name chosen during lobby confirmation
- `vote`: Vote submitted during Planning Poker phase

### Host Data
- `vote`: Vote submitted by the host during Planning Poker phase
