"use client";

import { useState } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Bell, BookMarked, History, Settings, Star } from "lucide-react";

export default function DashboardPage() {
  const [savedSearches] = useState([
    { id: 1, name: "Tech Industry Donations", type: "Donor Search" },
    { id: 2, name: "Healthcare Bills 2023", type: "Bill Search" },
  ]);

  const [recentActivity] = useState([
    { id: 1, action: "Viewed Profile", target: "Jane Smith", date: "2024-01-20" },
    { id: 2, action: "Saved Search", target: "Energy Bills", date: "2024-01-19" },
  ]);

  const [alerts] = useState([
    { id: 1, type: "Bill Update", message: "H.R. 1234 passed the House", date: "2024-01-20" },
    { id: 2, type: "Trade Alert", message: "New stock trade by John Doe", date: "2024-01-19" },
  ]);

  return (
    <div className="min-h-screen bg-muted/30">
      <div className="bg-background border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <h1 className="text-3xl font-bold">Dashboard</h1>
          <p className="text-muted-foreground mt-2">
            Welcome back! Manage your account and track your political insights.
          </p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Tabs defaultValue="overview" className="space-y-8">
          <TabsList>
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="saved">Saved Items</TabsTrigger>
            <TabsTrigger value="alerts">Alerts</TabsTrigger>
            <TabsTrigger value="settings">Settings</TabsTrigger>
          </TabsList>

          <TabsContent value="overview">
            <div className="grid gap-8 md:grid-cols-2">
              <Card>
                <CardHeader>
                  <div className="flex items-center space-x-2">
                    <History className="h-5 w-5 text-primary" />
                    <h2 className="text-xl font-semibold">Recent Activity</h2>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {recentActivity.map((activity) => (
                      <div key={activity.id} className="flex justify-between items-center">
                        <div>
                          <p className="font-medium">{activity.action}</p>
                          <p className="text-sm text-muted-foreground">{activity.target}</p>
                        </div>
                        <p className="text-sm text-muted-foreground">
                          {new Date(activity.date).toLocaleDateString()}
                        </p>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <div className="flex items-center space-x-2">
                    <Star className="h-5 w-5 text-primary" />
                    <h2 className="text-xl font-semibold">Saved Searches</h2>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {savedSearches.map((search) => (
                      <div key={search.id} className="flex justify-between items-center">
                        <div>
                          <p className="font-medium">{search.name}</p>
                          <p className="text-sm text-muted-foreground">{search.type}</p>
                        </div>
                        <Button variant="ghost" size="sm">View</Button>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </div>
          </TabsContent>

          <TabsContent value="saved">
            <Card>
              <CardHeader>
                <div className="flex items-center space-x-2">
                  <BookMarked className="h-5 w-5 text-primary" />
                  <h2 className="text-xl font-semibold">Saved Items</h2>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-6">
                  <div>
                    <h3 className="font-medium mb-4">Saved Politicians</h3>
                    <div className="grid gap-4 md:grid-cols-2">
                      {/* Add saved politicians list */}
                    </div>
                  </div>
                  <div>
                    <h3 className="font-medium mb-4">Saved Bills</h3>
                    <div className="grid gap-4 md:grid-cols-2">
                      {/* Add saved bills list */}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="alerts">
            <Card>
              <CardHeader>
                <div className="flex items-center space-x-2">
                  <Bell className="h-5 w-5 text-primary" />
                  <h2 className="text-xl font-semibold">Alerts</h2>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {alerts.map((alert) => (
                    <div key={alert.id} className="flex justify-between items-center border-b pb-4">
                      <div>
                        <p className="font-medium">{alert.type}</p>
                        <p className="text-sm text-muted-foreground">{alert.message}</p>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        {new Date(alert.date).toLocaleDateString()}
                      </p>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="settings">
            <Card>
              <CardHeader>
                <div className="flex items-center space-x-2">
                  <Settings className="h-5 w-5 text-primary" />
                  <h2 className="text-xl font-semibold">Account Settings</h2>
                </div>
              </CardHeader>
              <CardContent>
                <form className="space-y-6">
                  <div className="space-y-2">
                    <Label htmlFor="name">Name</Label>
                    <Input id="name" defaultValue="John Doe" />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <Input id="email" type="email" defaultValue="john@example.com" />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="notifications">Email Notifications</Label>
                    <div className="space-y-2">
                      <div className="flex items-center space-x-2">
                        <input type="checkbox" id="bill-updates" defaultChecked />
                        <Label htmlFor="bill-updates">Bill Updates</Label>
                      </div>
                      <div className="flex items-center space-x-2">
                        <input type="checkbox" id="trade-alerts" defaultChecked />
                        <Label htmlFor="trade-alerts">Trade Alerts</Label>
                      </div>
                    </div>
                  </div>
                  <Button>Save Changes</Button>
                </form>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}