using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Imaging;
using Microsoft.Win32;
using Newtonsoft.Json;
using Image = System.Drawing.Image;

namespace SplittedImageFilter
{
    /// <summary>
    /// Interaktionslogik für MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        private Bitmap image;
        private int width, height;
        private int bytesPerPixel;
        private int stride;
        private int numberOfBytes;
        private byte[] imgBytes;

        private Socket _serverSocket = new Socket(SocketType.Stream, ProtocolType.Tcp);
        private List<Socket> _clientSockets = new List<Socket>();
        private Dictionary<Socket, Rectangle> _socketJobs = new Dictionary<Socket, Rectangle>();
        private Dictionary<Rectangle, Bitmap> _snippets = new Dictionary<Rectangle, Bitmap>();
        private byte[] _buffer = new byte[1024 * 1024 * 20];

        public enum Filter { Red, Green, Blue, Median }

        public enum StripePosition { Bottom, Top, Sandwich, WholePicture }
        public Filter SelectedFilter { get; set; } = 0;

        public MainWindow()
        {
            InitializeComponent();
            _serverSocket.Bind(new IPEndPoint(IPAddress.Parse("0.0.0.0"), 55555));
            _serverSocket.Listen(1);
            _serverSocket.BeginAccept(AcceptCallback, null);
        }

        private void AcceptCallback(IAsyncResult ar)
        {
            Socket socket = _serverSocket.EndAccept(ar);
            _clientSockets.Add(socket);
            socket.BeginReceive(_buffer, 0, _buffer.Length, SocketFlags.None, ReceiveCallback, socket);
            Dispatcher?.Invoke(() => TextStatus.Text = $"Currently {_clientSockets.Count} connected");
        }

        private void ReceiveCallback(IAsyncResult ar)
        {
            Socket socket = (Socket)ar.AsyncState;
            try
            {
                int received = socket.EndReceive(ar);
                byte[] dataBuffer = new byte[received];
                Array.Copy(_buffer, dataBuffer, received);
                socket.BeginReceive(_buffer, 0, _buffer.Length, SocketFlags.None, ReceiveCallback, socket);
                //save snippet
                ImageConverter ic = new ImageConverter();
                Image img = (Image)ic.ConvertFrom(dataBuffer);
                Bitmap snippet = new Bitmap(img);

                _snippets.Add(_socketJobs[socket], snippet);

                //if all snippets arrived -> rebuild bitmap
                if (_snippets.Count == _clientSockets.Count)
                {
                    Application.Current.Dispatcher?.BeginInvoke((Action)(() =>
                    {
                        lblProgress.Content = "Filter applied!";
                        Bitmap result = ConcatSnippets();
                        outputImage.Source = BitmapToImageSource(result);
                        InvalidateVisual();
                    }));
                }
                else
                    Dispatcher?.Invoke(() => TextStatus.Text = $"Got {_snippets.Count} of {_clientSockets.Count} snippets from kneeechts");

            }
            catch (Exception e)
            {
                _clientSockets.Remove(socket);
                Dispatcher?.Invoke(() => TextStatus.Text = $"Currently {_clientSockets.Count} connected");
                socket.Close();
                socket.Dispose();
            }
        }

        private Bitmap ConcatSnippets()
        {
            var resultHeight = _snippets.Values.Sum(x => x.Height);

            var resultBitmap = new Bitmap(width, resultHeight);
            var g = Graphics.FromImage(resultBitmap);
            var localHeight = 0;
            foreach (var image in _snippets.OrderBy(x => x.Key.Y).Select(x => x.Value))
            {
                g.DrawImage(image, 0, localHeight);
                localHeight += image.Height;
            }

            return resultBitmap;
        }

        private void BtnOpenFile_Click(object sender, RoutedEventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog { Title = "Open Image", Filter = "Image File|*.bmp;" };
            if (openFileDialog.ShowDialog() == true)
            {
                image = new Bitmap(openFileDialog.FileName);
                var bitmapImage = BitmapToImageSource(image);
                inputImage.Source = bitmapImage;
                CalculateMainValues(bitmapImage);
            }
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

        private void CalculateMainValues(BitmapImage bitmapImage)
        {
            width = bitmapImage.PixelWidth;
            height = bitmapImage.PixelHeight;
            bytesPerPixel = bitmapImage.Format.BitsPerPixel / 8;
            stride = width * bytesPerPixel; //number of Bytes per row
            numberOfBytes = stride * height;
            imgBytes = new byte[stride * height];
            progressBar.Minimum = 0;
            progressBar.Maximum = width * height;
            lblProgress.Content = "Progress: 0 of " + numberOfBytes;
            bitmapImage.CopyPixels(imgBytes, stride, 0);
        }

        private void BtnApply_Click(object sender, RoutedEventArgs e)
        {
            SelectedFilter = (Filter)Convert.ToInt32(((MenuItem)sender).Tag);
            Radius = Convert.ToInt32(txtRadius.Text);
            var stripeHeightPerKneeecht = height / _clientSockets.Count;

            if (image != null)
            {
                outputImage.Source.Freeze();

                //split bitmap into parts and schupf to kneeechts
                for (int currentHeight = 0; currentHeight < height; currentHeight = +stripeHeightPerKneeecht)
                {
                    Bitmap stripe;
                    byte[] stripeBytes = new byte[1];
                    StripePosition stripePosition = StripePosition.Sandwich;
                    Rectangle currentRectangle = new Rectangle(0, currentHeight, width, stripeHeightPerKneeecht);
                    if (_clientSockets.Count > 1)
                    {
                        if (SelectedFilter == Filter.Median) //Zerschupfung für Medianfilter
                        {
                            if (currentHeight == 0) //nur unten dazuschupfen
                            {
                                stripe = image.Clone(
                                    new Rectangle(0, currentHeight, width, stripeHeightPerKneeecht + Radius),
                                    PixelFormat.Format32bppArgb);
                                stripePosition = StripePosition.Top;
                                Array.Copy(imgBytes, stripeBytes, (width * stripeHeightPerKneeecht + Radius * width) * bytesPerPixel);
                            }
                            else if (currentHeight + stripeHeightPerKneeecht + 1 >= height) //nur oben dazuschuckn
                            {
                                stripe = image.Clone(
                                    new Rectangle(0, currentHeight, width, stripeHeightPerKneeecht + Radius),
                                    PixelFormat.Format32bppArgb);
                                stripePosition = StripePosition.Bottom;

                                Array.Copy(imgBytes, imgBytes.Length - ((width * stripeHeightPerKneeecht + Radius * width) * bytesPerPixel), stripeBytes, 0, (width * stripeHeightPerKneeecht + Radius * width) * bytesPerPixel);
                            }
                            else //oben und unten dazuschupfen
                            {
                                stripe = image.Clone(
                                    new Rectangle(0, currentHeight - Radius, width, stripeHeightPerKneeecht + 2 * Radius),
                                    PixelFormat.Format32bppArgb);

                                Array.Copy(imgBytes,
                                    (currentHeight * width - Radius * width) * bytesPerPixel,
                                    stripeBytes,
                                    0,
                                    (stripeHeightPerKneeecht * width + 2 * Radius * width) * bytesPerPixel
                                    );
                            }
                        }
                        else //andere Filter normal zerschupfen
                        {
                            stripe = image.Clone(
                                new Rectangle(0, currentHeight, width, stripeHeightPerKneeecht),
                                PixelFormat.Format32bppArgb);
                        }
                    }

                    else
                    {
                        stripePosition = StripePosition.WholePicture;
                        stripe = image.Clone(
                            new Rectangle(0, currentHeight, width, height),
                            PixelFormat.Format32bppArgb);
                        stripeBytes = imgBytes;
                    }


                    //get bytes of image
                    //MemoryStream stream = new MemoryStream();
                    //stripe.Save(stream, ImageFormat.Bmp);
                    // stripeBytes = stream.ToArray();

                    var imageDTO = new ImageDTO
                    {
                        height = stripe.Height,
                        width = stripe.Width,
                        bytes = stripeBytes,
                        filter = (int)SelectedFilter,
                        radius = SelectedFilter == Filter.Median ? int.Parse(txtRadius.Text) : 0,
                        stripePosition = (int)stripePosition
                    };

                    string json = JsonConvert.SerializeObject(imageDTO);
                    byte[] schupf = Encoding.ASCII.GetBytes(json);


                    Socket socketForCurrentSchupf = _clientSockets[currentHeight / stripeHeightPerKneeecht];
                    _socketJobs.Add(socketForCurrentSchupf, currentRectangle);
                    //socketForCurrentSchupf.Send(BitConverter.GetBytes(schupf.Length));
                    Console.WriteLine(schupf.Length);
                    socketForCurrentSchupf.Send(schupf);
                }
            }
            else
            {
                MessageBox.Show("Nix is do!");
                MessageBox.Show("Minus!");
            }
        }

        public int Radius { get; set; }

        private void BtnExportFile_Click(object sender, RoutedEventArgs e)
        {
            //todo: export bitmap
        }
    }
}