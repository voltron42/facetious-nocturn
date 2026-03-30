# Facetious Nocturn Planning Poker Sequence Draft

This is meant to show the course of events in the Planning Poker app using a Facetious Nocturn session:

## Participants

* Host user
* Root page (planningPoker/index.html)
* Host app (planningPoker/host/index.html)
* Guest users (1 or more)
* Guest app (planningPoker/guest/index.html)
* Server

## Events

1. Host user triggers a new session on Root page
2. Server returns session-id and host-id to Root page
3. Root page re-routes to Host app
4. Host app provides Host user with a ui to enter their name and the names of the guests to be invited
   1. list can be dynamically added to
   2. all names (host and guests) must be unique
5. Host user fills out names and clicks "Lock In Session"
6. Host app submits update of session to server and begins polling "GET" for changes
   1. updates common data with "hostName" and "guestNames".
   2. "guestNames" will be a map where all the values are `false`
7. Host app switches to "Lobby" view while polling
   1. There is a button that copies the guest link into the clipboard
      1. "../guest/index.html?sessionId={sessionId}"
   2.  a list of the guests names is displayed
       1.  an icon will appear next to each guest as they accept their invitation
8. Host user will send the copied invite link to the would-be guests thru other means (chat, email) and wait
9. Guest users click the link and are taken to the guest page
   1.  The initial state of the guest page uses the "session GET" to get just the common data. it will display the host name and guest names and a "Confirm Invite" button
10. Guest user clicks "Confirm Invite"
11. Guest app sends "join" request to Server and holds on to guest-id when it is returned
12. Once the Guest app has the guest-id, it switches to "Lobby" view as well, and begins polling the "guest GET" for updates in "commonData"
    1.  the "Host Name" and "Guest Names" are displayed just like in the Host app "Lobby" view, except any "guest names" which have not been accepted will be a button for the Guest user to click to confirm their identity.
    2.  when a name button is clicked, the rest of the name buttons revert to just text and the "confirm" icon is applied to the name they chose. An update is also sent to the server to apply "name" to that guest's "fromGuest" data
13. As the Host app polls the "host GET", each time a new guest confirms their name in their "fromGuest" data, the Host app will submit an update to the "guestNames" in the "commonData" switching the value in the map from false to true.
14. Once all the values in the "GuestNames" map are showing as true
    1.  the Host app will 
        1.  stop polling for lobby updates
        2.  update the common data with a "votes" property that will be a map where the keys are all the names of the guests and the host, and the values in the map will initially all be `undefined`, and will 
        3.  switch to "PlanningPoker" view
        4.  begin polling for votes from the guests
    2.  the Guest app will
        1.  stop polling for lobby updates
        2.  switch to "PlanningPoker" view
        3.  begin polling for updates to "commonData"
15. From the "PlanningPoker" view, each user (Host and Guests) will click one of the icon buttons as their vote. From their individual view, they will each only be able to see their own vote until all votes have been submitted. The rest of the votes will be shown as blacked out
    1.  When a Guest user clicks on a vote icon button
        1.  the buttons all disappear
        2.  their vote appears next to their name on the list
        3.  the Guest app sends an update to the server, adding their "vote" to their "fromGuest" data
    2.  When the Host user clicks on a vote icon button
        1.  the buttons all disappear
        2.  their vote appears next to their name on the list
        3.  the Host app sends an update to the server, adding their "vote" to their "hostData" data
    3.  The Host app polls for updates to the "fromGuest" data for each guest.
        1.  Once all guests and the host have submitted their vote, the Host app sends an update to the server, updating the "votes" map with all of the votes from each user (host and guests)
        2.  when the update has been sent, the Host app will also display a "Clear for next vote" button
    4.  Once the Guest app receives the updated "votes" map in the "commonData" with all the new values, it will 
        1.  display all of the votes
        2.  begin polling for "clearing" property in "commonData" data
    5.  When the Host user clicks the "Clear for next vote" button, the Host app 
        1.  sends the following update
            1.  removing "vote" from "hostData"
            2.  returning the values in the "votes" map in the "commonData" to `undefined`
            3.  adding a "clearing" property the "commonData" with a value of `true`
        2.  begins polling for the "fromGuest" for all guests to no longer contain a vote
    6.  When the Guest app sees the "clearing" property in the "commonData" with a value of `true`, it will 
        1.  send an update to the server removing its "vote" from the "fromGuest" data
        2.  begin polling for "clearing" to no longer be present in the "commonData"
    7.  Once the Host app sees that all guests have removed the "vote" property from their "fromGuest" data, the Host app will 
        1.  send an update to the "commonData" removing the "clearing" property
        2.  begin polling for new votes (see point 15 above)
    8.  Once the Guest app sees that the "clearing" property has been removed from "commonData", it will stop polling for that and begin polling for new votes (see point 15 above)
