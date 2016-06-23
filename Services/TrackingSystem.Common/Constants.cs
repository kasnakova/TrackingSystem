namespace TrackingSystem.Common
{
    public static class Constants
    {
        public const int MinLengthStringField = 2;

        public const int MaxLengthStringField = 50;

        public const int MinLengthIdentifier = 10;

        public const int MaxLengthGCMKey = 255;

        public const double CoordiatePrecision = 0.0001;

        public const double MinLatitude = -90;

        public const double MaxLatitude = 90;

        public const double MinLongitude = -180;

        public const double MaxLongitude = 180;

        public const string TextNotValidTarget = "The target doesn't exist or is not yours!";

    }
}
