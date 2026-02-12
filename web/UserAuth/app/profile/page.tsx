"use client";

import { useEffect, useState } from "react";
import { User as UserIcon } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";

export default function Profile() {
    const [user, setUser] = useState<any>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchProfile = async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                alert("No token found, please log in.");
                window.location.href = "/";
                return;
            }

            try {
                const response = await fetch("http://localhost:8080/api/users/me", {
                    headers: {
                        "Authorization": `Bearer ${token}`,
                    },
                });

                const data = await response.json();

                if (response.ok) {
                    setUser(data);
                } else {
                    alert(data.message || "Failed to fetch user info");
                }
            } catch (error) {
                console.error(error);
                alert("Error fetching profile");
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, []);

    const handleLogout = () => {
        localStorage.removeItem("token");
        window.location.href = "/";
    };

    if (loading) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-black">
                <p className="text-gray-700 dark:text-gray-200 text-lg">Loading...</p>
            </div>
        );
    }

    if (!user) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-black">
                <p className="text-red-500 text-lg">No user data found</p>
            </div>
        );
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-zinc-50 dark:bg-black p-4">
            <Card className="w-[380px] border border-gray-300 dark:border-gray-700 shadow-lg">
                <CardHeader className="flex flex-col items-center bg-gradient-to-r from-indigo-500 to-purple-500 text-white rounded-t-lg p-6">
                    <Avatar className="w-24 h-24 mb-3">
                        {user.avatarUrl ? (
                            <AvatarImage src={user.avatarUrl} alt={user.username} />
                        ) : (
                            <AvatarFallback>
                                <UserIcon className="w-12 h-12 text-black" />
                            </AvatarFallback>
                        )}
                    </Avatar>
                    <CardTitle className="text-2xl font-semibold">{user.username}</CardTitle>
                    <CardDescription className="text-sm opacity-90 text-white">
                        {user.role}
                    </CardDescription>
                </CardHeader>

                <CardContent className="space-y-3 p-6">
                    <div className="flex justify-between">
                        <span className="font-medium">Email:</span>
                        <span className="text-gray-700 dark:text-gray-200">{user.email}</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="font-medium">Active:</span>
                        <span className="text-gray-700 dark:text-gray-200">{user.active ? "Yes" : "No"}</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="font-medium">Created At:</span>
                        <span className="text-gray-700 dark:text-gray-200">{new Date(user.createdAt).toLocaleString()}</span>
                    </div>
                    <div className="flex justify-between">
                        <span className="font-medium">Last Update:</span>
                        <span className="text-gray-700 dark:text-gray-200">{new Date(user.lastUpdate).toLocaleString()}</span>
                    </div>
                </CardContent>

                <CardFooter className="flex justify-center p-6">
                    <Button
                        variant="outline"
                        className="hover:bg-red-500 hover:text-white transition-colors"
                        onClick={handleLogout}
                    >
                        Logout
                    </Button>
                </CardFooter>
            </Card>
        </div>
    );
}
