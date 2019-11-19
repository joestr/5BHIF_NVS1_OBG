using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SplittedImageFilter
{
    public class ImageDTO
    {
        public int width { get; set; }
        public int height { get; set; }
        public byte[] bytes { get; set; }
        public int filter { get; set; }
        public int radius { get; set; } = 0;
        public int stripePosition { get; set; }
    }
}
