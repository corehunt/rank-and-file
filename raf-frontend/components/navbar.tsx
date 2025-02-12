"use client";

import { useState } from "react";
import Link from "next/link";
import { Scale, Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ThemeToggle } from "@/components/theme-toggle";
import { useRouter } from "next/navigation";
import {
  Sheet,
  SheetContent,
  SheetTrigger,
  SheetHeader,
  SheetTitle
} from "@/components/ui/sheet";

export default function Navbar() {
  const router = useRouter();
  const [isLoggedIn] = useState(false);

  // 1. Add state to control whether the sheet is open
  const [isOpen, setIsOpen] = useState(false);

  // Helper to close sheet & navigate
  const handleNavClick = (href: string) => {
    setIsOpen(false);       // Close the Sheet
    router.push(href);      // Then navigate to the link
  };

  return (
      <nav className="sticky top-0 z-50 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <Link href="/" className="flex items-center space-x-2">
                <Scale className="h-6 w-6 sm:h-8 sm:w-8" />
                <span className="font-bold text-lg sm:text-xl">Rank and File</span>
              </Link>
            </div>

            {/* Desktop Menu */}
            <div className="hidden md:flex items-center space-x-6">
              <Link href="/politicians" className="text-foreground/60 hover:text-foreground">
                Politicians
              </Link>
              <Link href="/bills" className="text-foreground/60 hover:text-foreground">
                Bills
              </Link>
              <Link href="/committees" className="text-foreground/60 hover:text-foreground">
                Committees
              </Link>
              <Link href="/donors" className="text-foreground/60 hover:text-foreground">
                Donors
              </Link>
              <Link href="/trading" className="text-foreground/60 hover:text-foreground">
                Trading
              </Link>
              <Link href="/about" className="text-foreground/60 hover:text-foreground">
                About Us
              </Link>
              <ThemeToggle />
              {/*{isLoggedIn ? (*/}
              {/*    <Button variant="outline" onClick={() => router.push("/dashboard")}>*/}
              {/*      Dashboard*/}
              {/*    </Button>*/}
              {/*) : (*/}
              {/*    <Button asChild>*/}
              {/*      <Link href="/auth">Sign In</Link>*/}
              {/*    </Button>*/}
              {/*)}*/}
            </div>

            {/* Mobile Menu */}
            <div className="flex md:hidden items-center space-x-2">
              <ThemeToggle />
              <Sheet
                  // 2. Control whether the sheet is open
                  open={isOpen}
                  onOpenChange={setIsOpen}
              >
                <SheetTrigger asChild>
                  <Button variant="ghost" size="icon" aria-label="Open menu">
                    <Menu className="h-5 w-5" />
                  </Button>
                </SheetTrigger>
                <SheetContent side="right" className="w-[300px] sm:w-[400px]">
                  <SheetHeader>
                    <SheetTitle>Navigation Menu</SheetTitle>
                  </SheetHeader>
                  <nav className="flex flex-col space-y-4 mt-6">
                    {/* 3. Close the sheet on link click and navigate */}
                    <Button
                        variant="ghost"
                        className="justify-start text-lg font-medium hover:text-primary px-0"
                        onClick={() => handleNavClick("/politicians")}
                    >
                      Politicians
                    </Button>
                    <Button
                        variant="ghost"
                        className="justify-start text-lg font-medium hover:text-primary px-0"
                        onClick={() => handleNavClick("/bills")}
                    >
                      Bills
                    </Button>
                    <Button
                        variant="ghost"
                        className="justify-start text-lg font-medium hover:text-primary px-0"
                        onClick={() => handleNavClick("/committees")}
                    >
                      Committees
                    </Button>
                    <Button
                        variant="ghost"
                        className="justify-start text-lg font-medium hover:text-primary px-0"
                        onClick={() => handleNavClick("/donors")}
                    >
                      Donors
                    </Button>
                    <Button
                        variant="ghost"
                        className="justify-start text-lg font-medium hover:text-primary px-0"
                        onClick={() => handleNavClick("/trading")}
                    >
                      Trading
                    </Button>
                    <Button
                        variant="ghost"
                        className="justify-start text-lg font-medium hover:text-primary px-0"
                        onClick={() => handleNavClick("/about")}
                    >
                      About Us
                    </Button>
                    {/*{isLoggedIn ? (*/}
                    {/*    <Button*/}
                    {/*        className="w-full"*/}
                    {/*        variant="outline"*/}
                    {/*        onClick={() => handleNavClick("/dashboard")}*/}
                    {/*    >*/}
                    {/*      Dashboard*/}
                    {/*    </Button>*/}
                    {/*) : (*/}
                    {/*    <Button*/}
                    {/*        className="w-full"*/}
                    {/*        variant="outline"*/}
                    {/*        onClick={() => handleNavClick("/auth")}*/}
                    {/*    >*/}
                    {/*      Sign In*/}
                    {/*    </Button>*/}
                    {/*)}*/}
                  </nav>
                </SheetContent>
              </Sheet>
            </div>
          </div>
        </div>
      </nav>
  );
}