Sample app showing how a fingerprint can be saved to the phone and later used to authenticate a user.

NOTES:

- The starting activity of the app is a LoginActivity with email and password fields. You can't actually sign in via email and password, but the rest of the activity's features function as intended (fingerprint authentication, sign in button enabling/disabling).
- By default, the fingerprint dialog immediately displays when the app is started and can be closed. If the dialog is closed, it can be brought back by clicking the fingerprint icon in the password field.
- If the phone doesn't have fignerprint scanning hardware or if there are no fingerprints stored on the device, the dialog will not automatically display. If the fingerprint icon is pressed, a dialog will show informing the user they cannot use fingerprint authentication without setting up fingerprints. The dialog has a button that takes the user to the Security & Locations activity of Settings.
- Once the user's fingerprint is authenticated, the welcome activity will start. Just to add some flavor, the welcome message will fade into the screen. The user can press the sign out button which will send them back to the login activity.
