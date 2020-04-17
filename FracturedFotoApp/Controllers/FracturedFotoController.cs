using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Web;
using System.Web.Mvc;

namespace FracturedFotoApp.Controllers
{
    public class FracturedFotoController : Controller
    {
        // GET: FracturedFoto
        public ActionResult Index()
        {
            return View();
        }

        public ActionResult AppsGallery() {

            return View();
        }

        public ActionResult AppsDetails()
        {
            var li = ListFiles();
            return View();
        }

        public ActionResult VideosGallery()
        {

            return View();
        }

        private List<string> ListFiles()
        {
            try
            {
                FtpWebRequest request = (FtpWebRequest)WebRequest.Create("ftp://waws-prod-pn1-007.ftp.azurewebsites.windows.net/site/wwwroot/Content");
                request.Method = WebRequestMethods.Ftp.ListDirectory;

                request.Credentials = new NetworkCredential("demoreports\\$demoreports", "toztqrfa1eWkS6KDhBETiFaaMlnxl97DAbDTohD3N1vkLoiEyDrpLhFLiu9p");
                FtpWebResponse response = (FtpWebResponse)request.GetResponse();
                Stream responseStream = response.GetResponseStream();
                StreamReader reader = new StreamReader(responseStream);
                string names = reader.ReadToEnd();

                reader.Close();
                response.Close();

                return names.Split(new string[] { "\r\n" }, StringSplitOptions.RemoveEmptyEntries).ToList();
            }
            catch (Exception)
            {
                throw;
            }
        }

    }
}