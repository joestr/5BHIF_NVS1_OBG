using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Newtonsoft.Json;

namespace Kneeecht
{
    /// <summary>
    /// Interaktionslogik für MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        private readonly Socket socket = new Socket(SocketType.Stream, ProtocolType.Tcp);

        private byte[] _buffer = new byte[1024 * 1024 * 20];
        public MainWindow()
        {
            InitializeComponent();

        }

        private void ButtonConnect_Click(object sender, RoutedEventArgs e)
        {
            image.Source?.Freeze();
            socket.Connect(IPAddress.Parse(TextIp.Text), Convert.ToInt32(TextPort.Text));
            socket.BeginReceive(_buffer, 0, _buffer.Length, SocketFlags.None, ReceiveCallback, socket);
        }

        private void ReceiveCallback(IAsyncResult ar)
        {
            Socket s = (Socket)ar.AsyncState;
            int received = s.EndReceive(ar);
            //Daten wurden empfangen und stehen im Buffer:
            byte[] realBuffer = new byte[received];
            Array.Copy(_buffer, realBuffer, received);
            var json = Encoding.UTF8.GetString(realBuffer);
            var imageDto = JsonConvert.DeserializeObject<ImageDTO>(json);
            //send back

            if (imageDto.filter == 1 || imageDto.filter == 2 || imageDto.filter == 3)
            {

                Bitmap bmp = MakeBitmap(imageDto.width, imageDto.height, imageDto.bytes);

                BitmapToImageSource(bmp);

                for (int y = 0; y < bmp.Height; y++)
                {
                    for (int x = 0; x < bmp.Width; x++)
                    {

                        System.Drawing.Color c = bmp.GetPixel(x, y);

                        System.Drawing.Color nC = System.Drawing.Color.FromArgb(
                            c.A,
                            imageDto.filter == 1 ? 0 : c.R,
                            imageDto.filter == 1 ? 0 : c.G,
                            imageDto.filter == 1 ? 0 : c.B

                            );
                        bmp.SetPixel(x, y, nC);


                    }
                }

                socket.Send(ImageToByte(bmp));
            }
            else if (imageDto.filter == 4)
            {

                Bitmap bmp = MakeBitmap(imageDto.width, imageDto.height, imageDto.bytes);

                int r = imageDto.radius;

                int runex = 0 + 1 + 2 * r;

                /**
                 * 0 -> bottom
                 * 1 -> top
                 * 2 -> sandwich
                 * 3 -> whole pic
                 */

                int tmpSPBottom = 0;
                int tmpSPTop = 0;

                if (imageDto.stripePosition == 0)
                {
                    tmpSPBottom = imageDto.radius;
                }

                if (imageDto.stripePosition == 1)
                {
                    tmpSPTop = imageDto.radius;
                }

                if (imageDto.stripePosition == 2)
                {
                    tmpSPBottom = imageDto.radius;
                    tmpSPTop = imageDto.radius;
                }

                for (int y = 0 + tmpSPBottom; y < imageDto.height - tmpSPTop; y++)
                {
                    for (int x = 0; x < imageDto.width; x++)
                    {
                        int[] bv = new int[runex * runex];
                        int[] rv = new int[runex * runex];
                        int[] gv = new int[runex * runex];
                        int addCount = 0;
                        for (int a = x - r; a < x + 1 + r; a++)
                        {
                            for (int b = y - r; b < y + 1 * r; b++)
                            {

                                if (a < 0 || b < 0) continue;

                                System.Drawing.Color color;
                                try
                                {
                                    color = bmp.GetPixel(a, b);
                                }
                                catch (Exception e)
                                {
                                    continue;
                                }

                                rv[addCount] = color.R;
                                gv[addCount] = color.G;
                                bv[addCount] = color.B;

                                addCount++;
                            }
                        }



                        int rm = (int)Median(rv);
                        int bm = (int)Median(bv);
                        int gm = (int)Median(gv);

                        for (int a = x - r; a < x + 1 + r; a++)
                        {
                            for (int b = y - r; b < y + 1 + r; b++)
                            {

                                if (a < 0 || b < 0) continue;

                                try
                                {
                                    System.Drawing.Color c = bmp.GetPixel(a, b);

                                    System.Drawing.Color nC = System.Drawing.Color.FromArgb(
                                        c.A,
                                        rm,
                                        bm,
                                        gm
                                        );
                                    bmp.SetPixel(x, y, nC);
                                }
                                catch (Exception e)
                                {

                                }
                            }
                        }

                    }
                }

                socket.Send(ImageToByte(bmp));
            }

            socket.BeginReceive(_buffer, 0, _buffer.Length, SocketFlags.None, new AsyncCallback(ReceiveCallback), socket);
        }

        /// <summary>
        /// https://stackoverflow.com/questions/39910710/converting-raw-byte-data-to-bitmap
        /// </summary>
        /// <param name="width"></param>
        /// <param name="height"></param>
        /// <param name="imageData"></param>
        /// <returns></returns>
        Bitmap MakeBitmap(int width, int height, byte[] imageData)
        {
            Bitmap rgw;
            byte[] newData = new byte[imageData.Length];

            for (int x = 0; x < imageData.Length; x += 4)
            {
                byte[] pixel = new byte[4];
                Array.Copy(imageData, x, pixel, 0, 4);

                byte r = pixel[2];
                byte g = pixel[1];
                byte b = pixel[0];
                byte a = pixel[3];

                byte[] newPixel = new byte[] { b, g, r, a };

                Array.Copy(newPixel, 0, newData, x, 4);
            }

            imageData = newData;

            using (var stream = new MemoryStream(imageData))
            using (var bmp = new Bitmap(width, height, System.Drawing.Imaging.PixelFormat.Format32bppArgb))
            {
                BitmapData bmpData = bmp.LockBits(new System.Drawing.Rectangle(0, 0,
                        bmp.Width,
                        bmp.Height),
                    ImageLockMode.WriteOnly,
                    bmp.PixelFormat);

                IntPtr pNative = bmpData.Scan0;
                Marshal.Copy(imageData, 0, pNative, imageData.Length);

                bmp.UnlockBits(bmpData);

                rgw = (Bitmap)bmp.Clone();
            }

            return rgw;
        }

        public BitmapImage BitmapToImageSource(Bitmap bitmap)
        {
            using (MemoryStream memory = new MemoryStream())
            {
                bitmap.Save(memory, ImageFormat.Bmp);
                memory.Position = 0;
                BitmapImage bitmapImage = new BitmapImage();
                bitmapImage.BeginInit();
                bitmapImage.StreamSource = memory;
                bitmapImage.CacheOption = BitmapCacheOption.OnLoad;
                bitmapImage.EndInit();
                return bitmapImage;
            }
        }


        byte[] ImageToByte(System.Drawing.Image img)
        {
            ImageConverter converter = new ImageConverter();
            return (byte[])converter.ConvertTo(img, typeof(byte[]));
        }

        private System.Drawing.Color GetMedianValue(Bitmap imageArg, int radius, int x, int y)
        {
            var c = imageArg.GetPixel(x, y);

            //get all pixels around c
            var startX = x - radius;
            var startY = y - radius;
            var width = 2 * radius + 1;
            var redValues = new List<int>();
            var greenValues = new List<int>();
            var blueValues = new List<int>();

            for (int i = startX; i < startX + width; i++)
            {
                for (int j = startY; j < startY + width; j++)
                {
                    try
                    {
                        redValues.Add(imageArg.GetPixel(i, j).R);
                        greenValues.Add(imageArg.GetPixel(i, j).G);
                        blueValues.Add(imageArg.GetPixel(i, j).B);
                    }
                    catch (Exception) //schiach
                    {
                        continue;
                    }
                }
            }

            //calculate medians for r,g,b
            int red = Median(redValues.ToArray());
            int green = Median(greenValues.ToArray());
            int blue = Median(blueValues.ToArray());
            return System.Drawing.Color.FromArgb(red, green, blue);
        }
        private static int Median(int[] values)
        {
            Array.Sort(values);
            int median, n = values.Length;
            if (n < 1) return values[0];
            else
            {
                if (n % 2 != 0)
                    median = values[(n + 1) / 2 - 1];
                else
                    median = Convert.ToInt32((values[n / 2 - 1] + values[n / 2]) / 2.0d);
            }
            return median;
        }
    }
}
