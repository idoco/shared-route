
var webSocket;
var sessionId = Math.floor((Math.random() * 10000000) + 1);
var serviceLocation = "sharedroute.cloudapp.net/app";
var taxiMap = {};
var taxiIcon;
var sharedTaxiIcon;
var map;
var userLocation;
var userMaker;
var isSharingLocation = false;
var shareRideButton;

function initialize() {
    var defaultLatLng = new google.maps.LatLng(32.078043, 34.774177); // Add the coordinates

    var mapOptions = {
        center: defaultLatLng,
        zoom: 14, // The initial zoom level when your map loads (0-20)
        minZoom: 13, // Minimum zoom level allowed (0-20)
        maxZoom: 17, // Maximum soom level allowed (0-20)
        zoomControl:false, // Set to true if using zoomControlOptions below, or false to remove all zoom controls.
        zoomControlOptions: {
            style:google.maps.ZoomControlStyle.DEFAULT // Change to SMALL to force just the + and - buttons.
        },
        mapTypeId: google.maps.MapTypeId.ROADMAP, // Set the type of Map
        scrollwheel: true, // Disable Mouse Scroll zooming (Essential for responsive sites!)
        // All of the below are set to true by default, so simply remove if set to true:
        panControl:false, // Set to false to disable
        mapTypeControl:false, // Disable Map/Satellite switch
        scaleControl:false, // Set to false to hide scale
        streetViewControl:false, // Set to disable to hide street view
        overviewMapControl:false, // Set to false to remove overview control
        rotateControl:false // Set to false to disable rotate control
    };
    var mapDiv = document.getElementById('map-canvas');
    map = new google.maps.Map(mapDiv, mapOptions);

    taxiIcon = new google.maps.MarkerImage(
        "https://raw.githubusercontent.com/idoco/shared-route/gh-pages/images/taxi_96x96.png",
        null, null, null, new google.maps.Size(30,30)); // Create a variable for our marker image.

    sharedTaxiIcon = new google.maps.MarkerImage(
        "https://raw.githubusercontent.com/idoco/shared-route/gh-pages/images/shared_taxi_96x96.png",
        null, null, null, new google.maps.Size(30,30)); // Create a variable for our marker image.

    userMaker = new google.maps.Marker({ // Set the marker
        position: defaultLatLng, // Position marker to coordinates
        map: map, // assign the marker to our map variable
        title: 'Me!'
    });

    var ctaLayer = new google.maps.KmlLayer({
        url: 'http://raw.githubusercontent.com/idoco/shared-route/master/map.kml'
    });
    ctaLayer.setMap(map);

    connectToServer();
    getLocation();

    google.maps.event.addListenerOnce(map, 'idle', function(){
        $('#loading-spinner').remove();
        $('#into-modal').openModal();
    });
}

function getLocation() {

    function newPosition(position) {
        userLocation = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
        userMaker.setPosition(userLocation);

        if (isSharingLocation) {
            sendMessage(userLocation.lat(), userLocation.lng());
        }
    }

    function positionError(err) {
        var message = 'Error(' + err.code + '): ' + err.message;
        alert(message);
    }

    if (navigator.geolocation) {
        var options = {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 5000
        };
        navigator.geolocation.watchPosition(newPosition, positionError, options);
    } else {
        alert("Geolocation is not supported by this browser.");
    }
}

function onMessageReceived(evt) {
    var msg = JSON.parse(evt.data); // native API
    var msgSessionId = msg["sessionId"];
    if (taxiMap[msgSessionId]){
        // Taxi id on map, updating marker location
        var newMarkerLatLng = new google.maps.LatLng(msg["lat"], msg["lng"]);
        var newMarker = taxiMap[msgSessionId];
        newMarker.setPosition(newMarkerLatLng)

    } else if (msgSessionId != sessionId) {
        // Taxi id not on map, adding new marker
        var markerLatLng = new google.maps.LatLng(msg["lat"], msg["lng"]);
        taxiMap[msgSessionId] = new google.maps.Marker({
            position: markerLatLng,
            icon: taxiIcon,
            map: map,
            title: 'Incoming Taxi!'
        });
    } else {
        // Taxi id is my taxi. Not adding to map
    }
}

function isNearRoute(lat, lng){
    return !(lat > 32.1009 || lat < 32.0550 || lng > 34.8068 || lng < 34.7596);
}

function sendMessage(lat, lng) {
    if (isNearRoute(lat, lng)){
        var msg = '{"sessionId":"' + sessionId + '", "lat":"' + lat + '", "lng":"' + lng + '"}';
        webSocket.send(msg);
    } else{
        console.info("User is not on route, filtering out location update " + lat + "," + lng);
    }
}

function connectToServer() {
    webSocket = new WebSocket('ws://'+serviceLocation);
    webSocket.onmessage = onMessageReceived;
    webSocket.onerror= function(error){
        alert('Error: '+error);
    };
}

function toggleSharingMode(){
    if (!shareRideButton) {
        return;
    }

    if (!isSharingLocation){
        Materialize.toast('Sharing your route', 3000);
        shareRideButton.className = "waves-effect waves-light btn red";
        shareRideButton.innerHTML = "<i class=\"mdi-maps-directions-bus right\"></i> Stop Sharing";
        userMaker.setIcon(sharedTaxiIcon);
        if (userLocation) {
            sendMessage(userLocation.lat(), userLocation.lng());
        }
        isSharingLocation = true;

    } else{
        Materialize.toast('Stopped sharing', 3000);
        shareRideButton.className = "waves-effect waves-light btn green";
        shareRideButton.innerHTML = "<i class=\"mdi-maps-directions-bus right\"></i> Share My Taxi";
        userMaker.setIcon(null);
        isSharingLocation = false;
    }
}

google.maps.event.addDomListener(window, 'load', initialize);

$( document ).ready(function() {
    $(".button-collapse").sideNav();
    shareRideButton = $("#share-ride-button")[0];
});