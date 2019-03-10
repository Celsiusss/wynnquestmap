// ==UserScript==
// @name         WynnQuestMap
// @namespace    http://tampermonkey.net/
// @version      0.1
// @description  try to take over the world!
// @author       Celsiuss
// @match        https://map.wynncraft.com/*
// @grant        none
// ==/UserScript==

(function() {
    'use strict';

    if (!window.L) return;
    if (!window.overviewer) return;
    if (!window.overviewerConfig) return;

    let map = null;

    setTimeout(() => map = window.overviewer.map, 1000);

    setInterval(() => {
        const httpRequest = new XMLHttpRequest();
    httpRequest.open('GET', 'http:/localhost:9090', true);
    httpRequest.send();
    httpRequest.onreadystatechange = handleResponse;

    function handleResponse() {
        if (httpRequest.readyState === XMLHttpRequest.DONE && httpRequest.status === 200) {
            const json = JSON.parse(httpRequest.responseText);
            if (!(json && json.quests)) return;

            let quests = json.quests.map(quest => {return {...quest, ...parseCoords(quest.coords)}});

            console.log(quests);

            quests.forEach((quest) => {
                console.log(quest);
            let marker = window.L.marker(quest.mapCoords, {icon: bookIcon}).addTo(map);
            marker.bindPopup(`<strong>${quest.name}</strong><br />${quest.description}`);
        });
        }
    }
}, 5000);


    const LeafIcon = window.L.Icon.extend({
        options: {
            shadowUrl: 'leaf-shadow.png',
            iconSize:     [50, 50],
            shadowSize:   [50, 64],
            iconAnchor:   [25, 25],
            shadowAnchor: [4, 62],
            popupAnchor:  [0, -25]
        }
    });
    const bookIcon = new LeafIcon({iconUrl: 'https://map.wynncraft.com/Content_Quest.png'});

    function parseCoords(coords) {
        coords = JSON.parse(coords);
        if (coords.length === 2) coords.splice(1, 0, 64);
        const tset = window.overviewerConfig.tilesets[0];
        const mapCoords = window.overviewer.util.fromWorldToLatLng(coords[0], coords[1], coords[2], tset);
        return {coords, mapCoords};
    }
})();
