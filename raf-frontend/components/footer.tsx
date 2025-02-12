import {Scale, Github, Twitter, Linkedin, Mail} from "lucide-react";
import Link from "next/link";

export default function Footer() {
  return (
    <footer className="border-t bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-12">
        {/* Mobile Layout */}
        <div className="md:hidden space-y-8">
          {/* Brand and Social Row */}
          <div className="text-center space-y-4">
            <div className="flex items-center justify-center space-x-2">
              <Scale className="h-6 w-6" />
              <span className="font-bold text-lg">Rank and File</span>
            </div>
            <p className="text-sm text-muted-foreground">
              Unlocking transparent insights into U.S. politics to empower your decisions.
            </p>
            <div className="flex justify-center space-x-4">
              <Link href="https://x.com/rankandfileus" className="text-muted-foreground hover:text-foreground">
                <Twitter className="h-5 w-5" />
              </Link>
              {/*<Link href="https://github.com/rankandfile" className="text-muted-foreground hover:text-foreground">*/}
              {/*  <Github className="h-5 w-5" />*/}
              {/*</Link>*/}
              <Link href="mailto:contact@rankandfile.us" className="text-muted-foreground hover:text-foreground">
                <Mail className="h-5 w-5" />
              </Link>
            </div>
          </div>

          {/* Navigation Links Row */}
          <div className="grid grid-cols-3 gap-8 text-center">
            <div>
              <h3 className="font-semibold mb-4">Explore</h3>
              <ul className="space-y-2">
                <li>
                  <Link href="/politicians" className="text-sm text-muted-foreground hover:text-foreground">
                    Politicians
                  </Link>
                </li>
                <li>
                  <Link href="/bills" className="text-sm text-muted-foreground hover:text-foreground">
                    Bills
                  </Link>
                </li>
                <li>
                  <Link href="/committees" className="text-sm text-muted-foreground hover:text-foreground">
                    Committees
                  </Link>
                </li>
                <li>
                  <Link href="/donors" className="text-sm text-muted-foreground hover:text-foreground">
                    Donors
                  </Link>
                </li>
                <li>
                  <Link href="/trading" className="text-sm text-muted-foreground hover:text-foreground">
                    Trading
                  </Link>
                </li>
              </ul>
            </div>

            <div>
              <h3 className="font-semibold mb-4">Resources</h3>
              <ul className="space-y-2">
                <li>
                  <Link href="/about" className="text-sm text-muted-foreground hover:text-foreground">
                    About Us
                  </Link>
                </li>
                <li>
                  <Link href="/methodology" className="text-sm text-muted-foreground hover:text-foreground">
                    Methodology
                  </Link>
                </li>
                {/*<li>*/}
                {/*  <Link href="/api" className="text-sm text-muted-foreground hover:text-foreground">*/}
                {/*    API Access*/}
                {/*  </Link>*/}
                {/*</li>*/}
                <li>
                  <Link href="/contact" className="text-sm text-muted-foreground hover:text-foreground">
                    Contact
                  </Link>
                </li>
                <li>
                  <Link href="/faq" className="text-sm text-muted-foreground hover:text-foreground">
                    FAQ
                  </Link>
                </li>
              </ul>
            </div>

            <div>
              <h3 className="font-semibold mb-4">Legal</h3>
              <ul className="space-y-2">
                <li>
                  <Link href="/privacy" className="text-sm text-muted-foreground hover:text-foreground">
                    Privacy Policy
                  </Link>
                </li>
                <li>
                  <Link href="/terms" className="text-sm text-muted-foreground hover:text-foreground">
                    Terms of Service
                  </Link>
                </li>
              </ul>
            </div>
          </div>
        </div>

        {/* Desktop Layout */}
        <div className="hidden md:grid md:grid-cols-4 gap-8">
          <div className="text-left space-y-4">
            <div className="flex items-center space-x-2">
              <Scale className="h-6 w-6" />
              <span className="font-bold text-lg">Rank and File</span>
            </div>
            <p className="text-sm text-muted-foreground">
              Unlocking transparent insights into U.S. politics to empower your decisions.
            </p>
            <div className="flex space-x-4">
              <Link href="https://x.com/rankandfileus" className="text-muted-foreground hover:text-foreground">
                <Twitter className="h-5 w-5" />
              </Link>
              {/*<Link href="https://github.com/rankandfile" className="text-muted-foreground hover:text-foreground">*/}
              {/*  <Github className="h-5 w-5" />*/}
              {/*</Link>*/}
              <Link href="mailto:contact@rankandfile.us" className="text-muted-foreground hover:text-foreground">
                <Mail className="h-5 w-5" />
              </Link>
            </div>
          </div>

          <div>
            <h3 className="font-semibold mb-4">Explore</h3>
            <ul className="space-y-2">
              <li>
                <Link href="/politicians" className="text-sm text-muted-foreground hover:text-foreground">
                  Politicians
                </Link>
              </li>
              <li>
                <Link href="/bills" className="text-sm text-muted-foreground hover:text-foreground">
                  Bills
                </Link>
              </li>
              <li>
                <Link href="/committees" className="text-sm text-muted-foreground hover:text-foreground">
                  Committees
                </Link>
              </li>
              <li>
                <Link href="/donors" className="text-sm text-muted-foreground hover:text-foreground">
                  Donors
                </Link>
              </li>
              <li>
                <Link href="/trading" className="text-sm text-muted-foreground hover:text-foreground">
                  Trading
                </Link>
              </li>
            </ul>
          </div>

          <div>
            <h3 className="font-semibold mb-4">Resources</h3>
            <ul className="space-y-2">
              <li>
                <Link href="/about" className="text-sm text-muted-foreground hover:text-foreground">
                  About Us
                </Link>
              </li>
              <li>
                <Link href="/methodology" className="text-sm text-muted-foreground hover:text-foreground">
                  Methodology
                </Link>
              </li>
              {/*<li>*/}
              {/*  <Link href="/api" className="text-sm text-muted-foreground hover:text-foreground">*/}
              {/*    API Access*/}
              {/*  </Link>*/}
              {/*</li>*/}
              <li>
                <Link href="/contact" className="text-sm text-muted-foreground hover:text-foreground">
                  Contact
                </Link>
              </li>
              <li>
                <Link href="/faq" className="text-sm text-muted-foreground hover:text-foreground">
                  FAQ
                </Link>
              </li>
            </ul>
          </div>

          <div>
            <h3 className="font-semibold mb-4">Legal</h3>
            <ul className="space-y-2">
              <li>
                <Link href="/privacy" className="text-sm text-muted-foreground hover:text-foreground">
                  Privacy Policy
                </Link>
              </li>
              <li>
                <Link href="/terms" className="text-sm text-muted-foreground hover:text-foreground">
                  Terms of Service
                </Link>
              </li>
            </ul>
          </div>
        </div>
        
        {/*<div className="mt-8 pt-8 border-t text-center">*/}
        {/*  <p className="text-sm text-muted-foreground">*/}
        {/*    © {new Date().getFullYear()} Rank and File. All rights reserved.*/}
        {/*  </p>*/}
        {/*</div>*/}
      </div>
    </footer>
  );
}