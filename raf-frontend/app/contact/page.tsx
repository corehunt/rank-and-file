"use client";

import { Card, CardContent, CardHeader } from "@/components/ui/card";
import Link from "next/link";

export default function ContactPage() {
    return (
        <div className="min-h-screen bg-muted/30">
            {/* Header */}
            <div className="bg-background border-b">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
                    <div className="text-center max-w-3xl mx-auto">
                        <h1 className="text-4xl font-bold mb-4">Contact Us</h1>
                        <p className="text-xl text-muted-foreground">
                            Have questions? We'd love to hear from you.
                        </p>
                    </div>
                </div>
            </div>

            {/* Merged Contact Card */}
            <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
                <Card>
                    <CardHeader>
                        <h2 className="text-2xl font-semibold text-center">Contact Us</h2>
                    </CardHeader>
                    <CardContent>
                        {/* Coming Soon Section */}
                        <div className="mb-6 text-center">
                            <p className="text-base sm:text-lg text-muted-foreground">
                                We're working on our contact form.
                                <br />
                                In the meantime, please email us at:{" "}
                                <a
                                    href="mailto:contact@rankandfile.us"
                                    className="text-primary underline"
                                >
                                    contact@rankandfile.us
                                </a>
                            </p>
                        </div>

                        {/* Common Questions Section */}
                        <div className="pt-6 border-t">
                            <h3 className="text-xl font-semibold text-center mb-4">
                                Common Questions
                            </h3>
                            <div className="space-y-4">
                                <div>
                                    <h4 className="font-medium">How often is the data updated?</h4>
                                    <p className="text-sm text-muted-foreground">
                                        Our bill data is updated hourly, daily for representative information, and all sourced from congress.gov.
                                    </p>
                                </div>
                                <div>
                                    <h4 className="font-medium">Can I access historical data?</h4>
                                    <p className="text-sm text-muted-foreground">
                                        Yes, our database includes historical records dating back to 1993.
                                    </p>
                                </div>
                                <div className="pt-2 text-center">
                                    <Link href="/faq" className="text-primary hover:underline">
                                        View all frequently asked questions →
                                    </Link>
                                </div>
                            </div>
                        </div>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}