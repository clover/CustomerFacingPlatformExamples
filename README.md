# ![logo](assets/clover_logo.png)

## Clover Customer-Facing Platform SDK - Example App

This repository contains an example application that showcases features of the new Clover CFP (Customer-Facing Platform) SDK.

### Overview

A custom activity is an Android activity that you can install on semi-integrated Clover devices in the customer-facing mode through USB Pay Display, Secure Network Pay Display, or Cloud Pay Display. Your POS can then communicate with this activity using the following `CloverConnector` methods:

|Method|Description  |
|--|--|
|`CloverConnector.startCustomActivity()`|Trigger a custom activity on the customer-facing Clover device|
| `CloverConnector.sendMessageToActivity()` |Send information to the custom activity|
| `CloverConnectorListener.onMessageFromActivity()` |Receive information from the custom activity|
| `CloverConnectorListener.onCustomActivityResult()` |Receive a result from the custom activity|

The `clover-cfp-sdk` provides a base implementation of an activity. You can extend the implementation to:

 - Set result values from a Custom Activity
 - Send and receive string messages between your POS and the Custom Activity running on the Clover device

With this SDK, you can trigger custom activities from your POS to a Clover device and create tailor-made customer experiences, such as tip selection, ratings and feedback, benefits program enrollment, and more. You can find the maven repository of the SDK [here](http://mvnrepository.com/artifact/com.clover.cfp/clover-cfp-sdk).

Please visit the [Clover Developer Docs](https://docs.clover.com/build/custom-activities-for-clover-mini/) for documentation and code snippets.

### Sample Application Setup

This sample application requires an Android device connected to a Clover device using USB Pay Display or Secure Network Pay Display. Note that this can be done with two Clover Minis as they natively run Android. To get started, clone the following repository:

`git clone https://github.com/clover/CustomerFacingPlatformExamples.git`

You can also simply download and extract the ZIP file.

#### Installing Projects

In the sample application package, you will see two projects, an `CustomActivityApp` project and `CustomActivities` project. You can begin working with these projects by performing the following tasks:

 1. Ensure that you have the Android device that will serve as your POS connected to your computer. Side-load the `CustomActivityApp` project on an Android device by running `gradle installDebug` in the project directory, or by running the project in Android Studio with the attached device set as your deployment target.
 2. Similarly, side-load the `CustomActivities` project on the Clover device. The CloverConnector methods mentioned previously require an activity name as a parameter, and will start one on the Clover device if it finds a matching activity.
 3. On the Clover device, run either **USB Pay Display** (when tethered) or **Secure Network Pay Display** (when connected to the same network) and press Start.
    <p align="center">
        <img src="assets/UPD.png" width="45%">
        <img src="assets/SNPD.png" width="45%">
    </p>

 4. Once installed, run `CustomActivityApp` from your Android device and connect using the appropriate Pay Display application.
    <p align="center">
        <img align="center" src="assets/connect.png" width="75%">
    </p>

### Features

#### Basic Example

- In the basic example, `CloverConnector.startActivity()` is used to send a JSON-like string payload to the Clover Device
- Pressing "Finish" within the activity invokes the `clover-cfp-sdk` method `setResultAndFinish()` which sends a payload back through `CloverConnector.onCustomActivityResult()`.
- This represents the beginning and end of a single Custom Activity **session**.
<p align="center">
    <img src="assets/basic.gif" width="50%">
</p>

#### Basic Conversation Example

- After starting a Custom Activity session, `CloverConnector.sendMessageToActivity()` is used to send a joke text to the Clover Device.
- Pressing "Send" within the activity invokes the `clover-cfp-sdk` method `sendMessage()` which sends a joke response payload back through `CloverConnector.onMessageFromActivity()`.
- Data can be exchanged like this indefinitely until the session is completed, when the activity invokes `setResultAndFinish()` and the POS receives the signal through `CloverConnector.onCustomActivityResult()`. In this example, only one joke/response is exchanged, and pressing "Send" will end the Custom Activity session.
<p align="center">
    <img src="assets/conversation.gif" width="50%">
</p>

#### Carousel Example

You can launch an activity to display a photo stream on the customer-facing Clover Device, like a customized welcome screen. You can touch the four corners of the device to exit to the main menu.
<p align="center">
    <img src="assets/carousel.gif" width="50%">
</p>

#### Web View Example

A web view is launched on the Clover device, and communicates with the POS to send back a message. 
<p align="center">
    <img src="assets/webview.gif" width="50%">
</p>

#### Ratings Example

- A review activity is launched on the Clover device for a customer to sign in and rate their dining experience.
- To test, use the code "1111." The activity sends back the authenticated customer's information to the POS.
- Proceeding through each question sends back the rating to the POS until the activity is completed.

<p align="center">
    <img src="assets/ratings.gif" width="50%">
</p>

#### NFC Example

An activity is launched on the Clover device, which enables it to listen for an NFC tap, and then to send the tagged serial number back to the POS (This example currently works only on Bypass Fortress Cards).
<p align="center">
    <img src="assets/nfc.png" width="50%">
</p>