﻿using Newtonsoft.Json;

namespace BehavioralAnalysisLog.Models
{
    public class Log<T> : RawLog
    {
        [JsonProperty("value")]
        public new virtual T Value { get; set; }
    }
}