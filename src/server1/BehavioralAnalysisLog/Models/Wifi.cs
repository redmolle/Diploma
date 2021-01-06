﻿using Newtonsoft.Json;

namespace server.Models
{
    public class Wifi : BaseEntity
    {
        [JsonProperty("BSSID")]
        public string BSSID { get; set; }

        [JsonProperty("SSID")]
        public string SSID { get; set; }
    }
}